/*
 * The MIT License
 *
 * Copyright 2022 Rza Asadov (rza dot asadov at gmail dot com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.acceix.frontend.crud.loaders;

import org.acceix.ndatabaseclient.DataTypes;
import org.acceix.frontend.crud.models.CrudField;
import org.acceix.frontend.crud.models.CrudFilterField;
import org.acceix.frontend.crud.models.CrudObject;
import org.acceix.frontend.crud.models.CrudTable;
import org.acceix.frontend.helpers.DbMetaData;
import org.acceix.frontend.web.commons.DataUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.ndatabaseclient.DataConnector;
import org.acceix.frontend.crud.interfaces.Container;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author zrid
 */
public class ObjectLoader implements Container<CrudObject> {
    
    private static Map<String,CrudObject> containerMap = new LinkedHashMap<>();
    
    
    public static Map<String,Object> ENVS;

    public static void setGlobalEnvs(Map<String, Object> ENVS) {
        ObjectLoader.ENVS = ENVS;
    }
     
    public boolean isExist(String obj) {
        return containerMap.containsKey(obj);
    }
    
    
    @Override
    public void add(CrudObject nCrudObject) {
        containerMap.put(nCrudObject.getName(), nCrudObject);
    }    
    
    @Override
    public CrudObject get(String obj) {
                return containerMap.get(obj);
    }
    
    public CrudObject get(int obj) {
        return (CrudObject)containerMap.get((String)containerMap.keySet().toArray()[obj]);
    }  
    
    @Override
    public List<CrudObject> getList() {
        
                CrudObject[] system_objects = new CrudObject[containerMap.size()];
                
                int index=0;
                for (Map.Entry<String,CrudObject> entry : containerMap.entrySet()) {
                    system_objects[index] = entry.getValue();
                    index++;
                }
                

                Arrays.sort(system_objects, Comparator.comparingLong(CrudObject::getTimeModified).reversed());        
                
                return Arrays.asList(system_objects);
                
    }    
    
    @Override
    public void load(File objfile) {
        

            if (objfile.exists() && objfile.canRead()) {

                try {

                    NLogger.logger(NLogBlock.OBJECTS,NLog.MESSAGE,"ObjectLoader","load","system","Parsed object file: " + objfile);

                    var nCrudObject = 
                            ObjectLoader.readObject(
                             (Map) new DataUtils().readJsonObjectFromString(
                                     new String ( 
                                             Files.readAllBytes(
                                                     Paths.get(objfile.toURI())))),objfile);

                    containerMap.put(nCrudObject.getName(), nCrudObject);
                    
                } catch (IOException ex) {
                    NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","load","system", "Unable to read Object from file=" + objfile.getAbsolutePath());
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","load","system", "Unable to parse Object from file=" + objfile.getAbsolutePath());
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);                    
                }

            }
      
    } 
     
    
    @Override
    public void loadAll(String path) {
        
        
            var objectsPath = new File(path);
            
            if (objectsPath.exists() && objectsPath.isDirectory()) {
                
                File[] files = objectsPath.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                        
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }
                
            } else {
                NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","loadAll","system","No objects folder on path: " + ENVS.get("objects_path"));
            }
            
    }    
    
    @Override
    public  void unload (String objname) {
        containerMap.remove(objname);   
    }
    
    @Override
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }      
    
    public static CrudObject readObject (Map jSONObject,File objfile) {
        
        
        CrudObject nCrudObject = new CrudObject();

        nCrudObject.setObjectType((String)jSONObject.getOrDefault("objectType",""));
        nCrudObject.setOperationType((String)jSONObject.getOrDefault("operationType",""));

        if (objfile == null) {
            nCrudObject.setFilepath(null);
            nCrudObject.setTimeModified(0);
        } else {
            nCrudObject.setFilepath(objfile.getAbsolutePath());
            nCrudObject.setTimeModified(objfile.lastModified());
        }
        

        nCrudObject.setName((String)jSONObject.get("name"));
        NLogger.logger(NLogBlock.OBJECTS,NLog.MESSAGE,"ObjectLoader","reset","system","Found object -> [" + nCrudObject.getName() +"]");        
        
        nCrudObject.setTitle((String)jSONObject.get("title"));
        nCrudObject.setCreatable((boolean)jSONObject.getOrDefault("creatable",Boolean.FALSE));
        nCrudObject.setEditable((boolean)jSONObject.getOrDefault("editable",Boolean.FALSE));
        nCrudObject.setRequireAuth((boolean)jSONObject.getOrDefault("requireAuth",Boolean.TRUE));
        
        nCrudObject.setRoleCreate((String)jSONObject.getOrDefault("roleCreate",""));
        nCrudObject.setRoleRead((String)jSONObject.getOrDefault("roleRead",""));
        nCrudObject.setRoleUpdate((String)jSONObject.getOrDefault("roleUpdate",""));
        nCrudObject.setRoleDelete((String)jSONObject.getOrDefault("roleDelete",""));
        
        nCrudObject.setTemplateForCreate((String)jSONObject.getOrDefault("templateForCreate","/defaultTemplates/createObjectData"));
        nCrudObject.setTemplateForRead((String)jSONObject.getOrDefault("templateForRead","/defaultTemplates/readObjectData"));
        nCrudObject.setTemplateForFiles((String)jSONObject.getOrDefault("templateForFiles","/defaultTemplates/readObjectFiles"));
        nCrudObject.setTemplateForLocation((String)jSONObject.getOrDefault("templateForLocation","/defaultTemplates/readObjectLocation"));

        nCrudObject.setTemplateForListRead((String)jSONObject.getOrDefault("templateForListRead","/defaultTemplates/readListObjectData"));
        nCrudObject.setTemplateForUpdate((String)jSONObject.getOrDefault("templateForUpdate","/defaultTemplates/updateObjectData"));
        nCrudObject.setTemplateForDelete((String)jSONObject.getOrDefault("templateForDelete","/defaultTemplates/deleteObjectData"));
        
        nCrudObject.setTemplateForFilters((String)jSONObject.getOrDefault("templateForFilters","/defaultTemplates/readObjectDataFilters"));
        
  
        
        // Get Table data
        
        Map<String,Map> JSONObject_tables =  (Map) jSONObject.get("tables");
        
        if (JSONObject_tables != null) {
        
            JSONObject_tables.entrySet()
                             .stream()
                             .filter((entry) -> !(entry.getKey().startsWith("#")))
                             .forEachOrdered((var entry) -> {

                                    NLogger.logger(NLogBlock.OBJECTS,NLog.MESSAGE,"ObjectLoader","readObject","system","Found table \"" + entry.getKey() + "\" in object [" + nCrudObject.getName() + "]");                

                                    CrudTable nCrudTable = readTable(nCrudObject.getName(),entry.getKey(),entry.getValue());
                                    nCrudObject.addCrudTable(entry.getKey(), nCrudTable);

                            });
            
        }
        // Get metadata fields
            
        Map JSONObject_metadata =  (Map) jSONObject.get("metadata");
        
        if (JSONObject_metadata != null) {
        
            JSONObject_metadata.keySet().forEach((key) -> {
                NLogger.logger(NLogBlock.OBJECTS,NLog.MESSAGE,"ObjectLoader","readObject","system","Found metadata \"" + key + "\" in object [" + nCrudObject.getName() + "]");                
                nCrudObject.addMetaData((String) key, JSONObject_metadata.get(key));
            });
            
        }
        
        return nCrudObject;
        
        
    }


    private static CrudTable readTable (String objectName,String tableName,Map table) {
        
            final DbMetaData dataMetadata = new DbMetaData(new DataConnector(ENVS,"system"));
            
            CrudTable nCrudTable = new CrudTable();
            
            nCrudTable.setName(tableName);

            // Parse table fields
            Map<String,Map> table_fields = (Map)table.getOrDefault("fields",new HashMap<>());
            
            table_fields.entrySet()
                        .stream()
                        .filter((entry) -> !(entry.getKey().startsWith("#")))
                        .forEachOrdered((var entry) -> {
                            // ignore commented
                            nCrudTable.addField(readTableField(entry.getKey(),objectName,tableName,entry.getValue(),dataMetadata));
                        });
            
            
            /////////////////////////////////////////// Parse table filters ////////////////////////////////////
            Map table_filters_read = (Map)table.getOrDefault("filters-read",new HashMap<>());
            table_filters_read.keySet().forEach((var key) -> { 
                CrudFilterField crudFilterField = readTableFilterReadFieldFromJson((String)key,
                                                                                            (Map)table_filters_read.get(key));

                crudFilterField.setfilterId((String)key);
                nCrudTable.addFilterForRead(crudFilterField); 
            });
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            

            ///////////////////// Parse request order by columns and direction /////////////////////////////////
            
            Map table_orderby_columns = (Map)table.getOrDefault("order-by",new HashMap<>());
            
            nCrudTable.setReadLimit((String)table.getOrDefault("read-limit","5000"));
            
            
                table_orderby_columns.keySet().forEach((key) -> {
                    
                    if (key.toString().equals("asc")) {
                        nCrudTable.setOrderByList((List)table_orderby_columns.get(key));
                        nCrudTable.setOrderByDirection(CrudTable.ORDER_BY_ASC);
                    } else if (key.toString().equals("desc")) {
                        nCrudTable.setOrderByList((List)table_orderby_columns.get(key));
                        nCrudTable.setOrderByDirection(CrudTable.ORDER_BY_DESC);
                    }

                });
            //////////////////////////////////////////////////////////////////////////////////////////////////
     
            
            ////////////////////////////////////// Parse ID field block //////////////////////////////////////
            
            Map table_id_field = (Map)table.get("id-field");
            
            if (table_id_field !=null) 
                table_id_field.keySet().forEach((key) -> {
                    
                    if (key.toString().toLowerCase().equals("id")) {
                         
                        NLogger.logger(NLogBlock.OBJECTS,NLog.MESSAGE,"ObjectLoader","readTable","system","Found id field -> \"" + table_id_field.get(key) + "\" in table \"" + tableName + "\"");
                        
                        nCrudTable.setIdFieldName((String)table_id_field.get(key));
                        

                        
                        if (dataMetadata.getColumnList(tableName)!=null) {
                            
                            Map<String,Object> idFieldsColumnMetaData = dataMetadata.getColumn(tableName,nCrudTable.getIdFieldName());
                            if (idFieldsColumnMetaData!=null) {
                                if (table_id_field.get("dataType")==null) {
                                    nCrudTable.setIdFieldDataType((int)idFieldsColumnMetaData.get("dataType"));
                                } else {
                                    nCrudTable.setIdFieldDataType(new DataTypes().stringToDataType((String)table_id_field.get("dataType")));
                                }
                            } else {
                                NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","readTable","system","It seems we have miss id field in  object \"" + objectName + "\"'s table \"" + tableName + "\" but do not have field in database !");
                            }
                        } else {
                            NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","readTable","system","It seems we have id field in  object \"" + objectName + "\"'s table \"" + tableName + "\" but do not have such column in database !");
                        }
                        
                    } else if (key.toString().toLowerCase().equals("listidfield")) {
                        nCrudTable.setListIdField((String)table_id_field.get(key));
                    }

                });
            
            String addQuery = (String)table.get("addQuery");
            
            if (addQuery != null) {
                nCrudTable.setAddQuery(addQuery);
            }
            
            return nCrudTable;
            
    }
    
    
    private static CrudFilterField readTableFilterReadFieldFromJson (String fieldName,Map table_field) {
        
                    CrudFilterField nFilterField = new CrudFilterField();
                    
                    if (fieldName.contains(":")) {
                        nFilterField.setFieldName(fieldName.split(":")[0]);
                    } else {
                        nFilterField.setFieldName(fieldName);
                    }

                    nFilterField.setFieldValue(table_field.getOrDefault("value",null));
                    
                    nFilterField.setCondition((String)table_field.get("condition"));
                    
                    nFilterField.setType((String)table_field.getOrDefault("type","exported"));


                    return nFilterField;
        
    }    

    
    private static CrudField readTableField (String tableField,String objectName,String tableName, Map tableFields, DbMetaData dbMetaData) {

                    CrudField nCrudField = new CrudField();
                    nCrudField.setGlobalEnvs(ENVS);

                    nCrudField.setFieldKey(tableField.toLowerCase());
                    nCrudField.setObject(objectName);
                    nCrudField.setTable(tableName);
                    
                    nCrudField.setDisplayName((String)tableFields.getOrDefault("displayName",nCrudField.getFieldKey()));

                    nCrudField.setUseForAdd((boolean)tableFields.getOrDefault("useForAdd", Boolean.FALSE));                    
                    nCrudField.setListData((boolean)tableFields.getOrDefault("isListData",Boolean.FALSE));
                
                    /*if (nCrudField.isList()) {
                        nCrudField.setEditable(false);
                        nCrudField.setCreatable(false);
                        nCrudField.setViewable(false);
                    } */                    

                    nCrudField.setEditable((boolean)tableFields.getOrDefault("isEditable",Boolean.TRUE));
                    nCrudField.setCreatable((boolean)tableFields.getOrDefault("isCreatable",Boolean.TRUE));
                    nCrudField.setViewable((boolean)tableFields.getOrDefault("isViewable",Boolean.TRUE));


                    nCrudField.setMandatory((boolean)tableFields.getOrDefault("isMandatory",Boolean.TRUE));                  
                    nCrudField.setDefaultVaue((Object)tableFields.getOrDefault("defaultValue",null));
                    nCrudField.setTestValue((Object)tableFields.getOrDefault("testValue",null));
                    nCrudField.setCrypted((boolean)tableFields.getOrDefault("isCrypted",false));

                    nCrudField.setLength((long)tableFields.getOrDefault("length",Long.parseLong("255")));
                    nCrudField.setFormat((String)tableFields.getOrDefault("format","dd/MM/yyyy"));
                    nCrudField.setFileFormat((String)tableFields.getOrDefault("fileFormat",null));
                    nCrudField.setPath((String)tableFields.getOrDefault("path","null"));
                    nCrudField.setApiKey((String)tableFields.getOrDefault("apiKey","null"));
                    nCrudField.setDefaultCountry((String)tableFields.getOrDefault("defaultCountry","null"));
                    nCrudField.setDefaultCity((String)tableFields.getOrDefault("defaultCity","null"));

                    
                    
                    
                    if (tableFields.get("dataType")!=null) {
                        nCrudField.setDataType(new DataTypes().stringToDataType((String)tableFields.getOrDefault("dataType","unknown")));
                    }
                    

                    
                    nCrudField.setExternalForCreate((boolean)tableFields.getOrDefault("useExternalForCreate",Boolean.TRUE));

                    if (tableFields.getOrDefault("externalObject",null) != null) {
                        
                            nCrudField.setExternal(Boolean.TRUE);                        
                            nCrudField.setExternalObject((String)tableFields.getOrDefault("externalObject",null));
                            nCrudField.setExternalTable((String)tableFields.getOrDefault("externalTable",null));
                            nCrudField.setExternalAddQuery((String)tableFields.getOrDefault("externalAddQuery",null));

                    }
                    
                    nCrudField.setExternalGetField((String)tableFields.getOrDefault("externalGetField",null));
                    nCrudField.setExternalJoinField((String)tableFields.getOrDefault("externalJoinField",null));
                    
                    nCrudField.setStatementRead((String)tableFields.getOrDefault("readStatement",null));
                    nCrudField.setStatementCreate((String)tableFields.getOrDefault("createStatement",null));
                    nCrudField.setStatementUpdate((String)tableFields.getOrDefault("updateStatement",null));

                                        
                    
                    if ((List) tableFields.getOrDefault("enumValues",null) != null) {
                    
                        List enumValues = (List) tableFields.get("enumValues");
                        enumValues.forEach((val) -> {
                            nCrudField.addEnumValue((String)val);
                        });
                    
                    }


                    
                        if (dbMetaData.getColumn(nCrudField.getTable(), nCrudField.getFieldName()) != null) {

                                if (nCrudField.getEnumValues().size() > 0) {
                                    nCrudField.setDataType(DataTypes.TYPE_ENUM);
                                } else {
                                    if (nCrudField.getDataType()==-1) {
                                        nCrudField.setDataType((int)dbMetaData.getColumn(nCrudField.getTable(), nCrudField.getFieldName()).get("dataType"));
                                    }
                                }

                                NLogger.logger(NLogBlock.OBJECTS,NLog.MESSAGE,"ObjectLoader","readTableField","system","Found field \"" + nCrudField.getFieldKey() + "\"" + " in object " + nCrudField.getObject() + " ] sql datatype is " + new DataTypes().dataTypeToString((int)dbMetaData.getColumn(nCrudField.getTable(), nCrudField.getFieldName()).get("dataType")));
                        } else {
                                NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","readTableField","system","It seems we have object [" + nCrudField.getObject() + "] but have not table [" + nCrudField.getTable() + "] in database !");
                        }
                        
                
                        if (nCrudField.isExternal()) {

                            if (nCrudField.getExternalGetField().contains(",")) {
                                    nCrudField.setExternalFieldDataType(DataTypes.TYPE_STRING);
                            } else {

                                    if (dbMetaData.getColumnList(nCrudField.getExternalTable())==null) {
                                        NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","readTableField","system","Object [" + nCrudField.getObject() + "] has a field with reference to external table \"" + nCrudField.getExternalTable() +"\" , but table absent in database !");
                                    } else {
                                        Map<String,Object> columnMetaDataOfExternalGetField = dbMetaData.getColumnList(nCrudField.getExternalTable()).get(nCrudField.getExternalGetField());
                                        if (columnMetaDataOfExternalGetField==null) {
                                            NLogger.logger(NLogBlock.OBJECTS,NLog.ERROR,"ObjectLoader","readTableField","system","Object [" + nCrudField.getObject() + "] has reference to external field \"" + nCrudField.getExternalGetField() + "\" of table \"" + nCrudField.getExternalTable() + "\" but field absent in database table !");
                                        } else {
                                            //System.out.println("Table1: " + tableName + " ETable:" + nCrudField.getExternalTable() + ", Field:" + nCrudField.getFieldName() + ", dt: " + (int)columnMetaDataOfExternalGetField.get("dataType"));
                                            nCrudField.setExternalFieldDataType((int)columnMetaDataOfExternalGetField.get("dataType"));
                                        }
                                    }

                            }
                        }  
                        
            return nCrudField;
    }
    

}