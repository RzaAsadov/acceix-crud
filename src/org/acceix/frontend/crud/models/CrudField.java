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

package org.acceix.frontend.crud.models;

import org.acceix.frontend.crud.loaders.ObjectLoader;
import org.acceix.frontend.helpers.NCodeButtons;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import org.acceix.ndatabaseclient.DataTypes;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.acceix.ndatabaseclient.DataConnector;
import org.acceix.ndatabaseclient.MachineDataSet;

/**
 *
 * @author zrid
 */
public class CrudField {
    
        public static int DOCUMENT_SECURITY_PRIVATE=1;
        public static int DOCUMENT_SECURITY_PUBLIC=2;
        public static int DOCUMENT_SECURITY_VIEWONCE=3;
        
    
        private Map<String,Object> globalEnvs;
        
        private String username;

        private String fieldName;
        private String fieldKey;
        
        private String table;
        private String object;
        
        private long orderId = 0;
        
        private int dataType = -1;
        
        private final List<String> enumValues = new LinkedList<>();
        
        private Object defaultVaue = null;
        private Object testValue = null;

        private boolean editable;
        private boolean createtable;
        private boolean viewable;
        private boolean mandatory;
        private boolean listData;
        private boolean useForAdd;
        
        private boolean crypted;

        
        private int fileStatus=0;
        
        private long maxFileCount=0;
        private long minFileCount=0;
        
        private int maxSize=0;
        
        private String keepSize="o";
        
        
        private String statementRead;
        private String statementCreate;
        private String statementUpdate;
        private boolean external;
        private boolean externalForCreate;
        private boolean externalNoChoiceOption;
        private String externalObject;
        private String externalTable;
        private String externalGetField;
        private int externalFieldDataType;        
        private String externalJoinField;
        private String externalAddQuery;
        private long length;
        private String format;
        private String fileFormat;
        private String path;
        private String apiKey;
        private String defaultCountry;
        private String defaultCity;

        
        private String displayName;

        public CrudField() {
            
        }

        public String getUsername() {
            return username;
        }



        public Map<String, Object> getGlobalEnvs() {
            return globalEnvs;
        }

        public void setGlobalEnvs(Map<String, Object> globalEnvs) {
            this.globalEnvs = globalEnvs;
        }



        public CrudField(String fieldName, int dataType, Object defaultVaue, boolean editable, boolean mandatory, boolean external, int length) {
            this.fieldName = fieldName;
            this.dataType = dataType;
            this.defaultVaue = defaultVaue;
            this.editable = editable;
            this.mandatory = mandatory;
            this.external = external;
            this.length = length;
        }
        
        public CrudField(String fieldName, String fieldKey,int dataType) {
            this.fieldName = fieldName;
            this.fieldKey = fieldKey;
            this.dataType = dataType;
        }        

        public void setTable(String table) {
            this.table = table;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public String getTable() {
            return table;
        }

        public String getObject() {
            return object;
        }




        
        public String getExternalAddQuery() {
            return externalAddQuery;
        }

        public void setExternalAddQuery(String externalAddQuery) {
            this.externalAddQuery = externalAddQuery;
        }

        public void setFieldKey(String fieldKey) {
            

            
            if (fieldKey.contains(":")) {
                setFieldName(fieldKey.split(":")[0]);
            } else {
                setFieldName(fieldKey);
            }           
            
            this.fieldKey = fieldKey.replace(':', '_');
        }

        public String getFieldKey() {
            return fieldKey;
        }

        public String getFileFormat() {
            return fileFormat;
        }

        public void setFileFormat(String fileFormat) {
            this.fileFormat = fileFormat;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public int getFileStatus() {
            return fileStatus;
        }

        public void setFileStatus(String fileStatus) {
            if (fileStatus.equals("private"))
                this.fileStatus = 1;
            else if (fileStatus.equals("public"))
                this.fileStatus = 2;
            else if (fileStatus.equals("viewonce"))
                this.fileStatus = 3;
        }

        public void setMaxFileCount(long maxFileCount) {
            this.maxFileCount = maxFileCount;
        }

        public long getMaxFileCount() {
            return maxFileCount;
        }
        
        

        public void setMinFileCount(long minFileCount) {
            this.minFileCount = minFileCount;
        }

        public long getMinFileCount() {
            return minFileCount;
        }

        

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getMaxSize() {
            return maxSize;
        }


        
        public void setKeepSize(String keepSize) {
            this.keepSize = keepSize;
        }

        public String getKeepSize() {
            return keepSize;
        }



        public boolean isUseForAdd() {
            return useForAdd;
        }

        public void setUseForAdd(boolean useForAdd) {
            this.useForAdd = useForAdd;
        }

        public boolean isCrypted() {
            return crypted;
        }

        public void setCrypted(boolean crypted) {
            this.crypted = crypted;
        }




        public String getFieldName() {
            return fieldName;
        }

        public void setOrderId(long orderId) {
            this.orderId = orderId;
        }

        public long getOrderId() {
            return orderId;
        }
        

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDataType() {
            return dataType;
        }

        public Object getDefaultValue() {
            return defaultVaue;
        }

        public void setTestValue(Object testValue) {
            this.testValue = testValue;
        }

        public Object getTestValue() {
            return testValue;
        }
        

        public boolean isEditable() {
            return editable;
        }

        public boolean isViewable() {
            return viewable;
        }

        public boolean isCreatable() {
            return createtable;
        }

        public void setCreatable(boolean createtable) {
            this.createtable = createtable;
        }

        public boolean isListData() {
            return listData;
        }

        public void setListData(boolean list) {
            this.listData = list;
        }


        public boolean isMandatory() {
            return mandatory;
        }

        public void setExternal(boolean external) {
            this.external = external;
        }

        public boolean isExternal() {
            return external;
        }

        public void setStatementRead(String statement) {
            this.statementRead = statement;
        }

        public String getStatementRead() {
            return statementRead;
        }

        public void setStatementCreate(String statementCreate) {
            this.statementCreate = statementCreate;
        }

        public String getStatementCreate() {
            return statementCreate;
        }

        public void setStatementUpdate(String statementUpdate) {
            this.statementUpdate = statementUpdate;
        }

        public String getStatementUpdate() {
            return statementUpdate;
        }
        
        

        public void setExternalForCreate(boolean externalForCreate) {
            this.externalForCreate = externalForCreate;
        }
        
        public boolean isExternalForCreate() {
            return externalForCreate;
        }        

        public void setExternalNoChoiceOption(boolean externalNoChoiceOption) {
            this.externalNoChoiceOption = externalNoChoiceOption;
        }

        public boolean isExternalNoChoiceOption() {
            return externalNoChoiceOption;
        }





        public void setExternalFieldDataType(int externalFieldDataType) {
            this.externalFieldDataType = externalFieldDataType;
        }
        
        /*public void setExternalFieldDataType(String dataType) {
                setExternalFieldDataType(new NDataTypes().stringToDataType(dataType));
        }*/         

        public int getExternalFieldDataType() {
            return externalFieldDataType;
        }

        public void setExternalObject(String externalObject) {
            this.externalObject = externalObject;
        }

        public String getExternalObject() {
            return externalObject;
        }

        public void setExternalTable(String externalTable) {
            this.externalTable = externalTable;
        }

        public String getExternalTable() {
            return externalTable;
        }



        public void setExternalGetField(String externalGetField) {
            this.externalGetField = externalGetField;
        }

        public String getExternalGetField() {
            return externalGetField;
        }

        public void setExternalJoinField(String externalJoinField) {
            this.externalJoinField = externalJoinField;
        }

        public String getExternalJoinField() {
            return externalJoinField;
        }

        public long getLength() {
            return length;
        }

        
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public void setDataType(int dataType) {
            this.dataType = dataType;
        }
        
        public String getDataTypeAsString() {
            
                return new DataTypes().dataTypeToString(getDataType());

        }
        
        public String getExternalFieldDataTypeAsString() {
            
                return new DataTypes().dataTypeToString(getExternalFieldDataType());
                
        }        
    
        /*private void setDataType(String dataType) {
            
                setDataType(new NDataTypes().stringToDataType(dataType));

        } */       


        public void setDefaultVaue(Object defaultVaue) {
            this.defaultVaue = defaultVaue;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public void setViewable(boolean viewable) {
            this.viewable = viewable;
        }
        

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }


        public void setLength(long length) {
            this.length = length;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setDefaultCountry(String defaultCountry) {
            this.defaultCountry = defaultCountry;
        }

        public String getDefaultCountry() {
            return defaultCountry;
        }

        public void setDefaultCity(String defaultCity) {
            this.defaultCity = defaultCity;
        }

        public String getDefaultCity() {
            return defaultCity;
        }




        
        public void addEnumValue(String value) {
            enumValues.add(value);
        }
        
        public List<String> getEnumValues() {
            return enumValues;
        }
        
        public void crudfieldToColumn(Map<Object,Object> columns,MachineDataSet machineDataSet,CrudObject crudObject, CrudTable crudTable) throws Exception {
        
                int index = columns.size();
                
                
                Integer datatype = getDataType();

                if (datatype != DataTypes.TYPE_DOCUMENT && datatype != DataTypes.TYPE_LOCATION) { 
                    datatype = machineDataSet.getColumnType(getFieldKey());
                }
                
                if (datatype==DataTypes.UNKNOWN_TYPE) {
                    throw new Exception("Unknown column: " + getFieldKey());
                } else if (datatype==DataTypes.TYPE_STRING) {
                    if (machineDataSet.getString(getFieldKey())==null) {
                        columns.put(index,"null");
                    } else {
                        columns.put(index,machineDataSet.getString(getFieldKey()));
                    }
                } else if (datatype==DataTypes.TYPE_INT) {
                    columns.put(index,machineDataSet.getInteger(getFieldKey()));
                } else if (datatype==DataTypes.TYPE_LONG) {
                    columns.put(index,machineDataSet.getLong(getFieldKey()));
                } else if (datatype==DataTypes.TYPE_FLOAT) {
                    columns.put(index,machineDataSet.getFloat(getFieldKey()));
                } else if (datatype==DataTypes.TYPE_DOUBLE) {
                    columns.put(index,machineDataSet.getDouble(getFieldKey()));
                } else if (datatype==DataTypes.TYPE_LONG) {
                    columns.put(index,machineDataSet.getLong(getFieldKey()));
                } else if (datatype==DataTypes.TYPE_BOOLEAN) {
                    columns.put(index,machineDataSet.getBoolean(getFieldKey()));
                }  else if (datatype==DataTypes.TYPE_TIMESTAMP) {
                    if (machineDataSet.getTimestamp(getFieldKey())==null) {
                        columns.put(index,"null");
                    } else {
                        columns.put(index,new SimpleDateFormat(getFormat()).format(machineDataSet.getTimestamp(getFieldKey()).getTime()));
                    }
                } else if (datatype==DataTypes.TYPE_JSON) {
                    columns.put(index,String.valueOf(machineDataSet.getBytes(getFieldKey())));
                } else if (datatype==DataTypes.TYPE_DOCUMENT) {
                                     

                        var button_view = new NCodeButtons().createButton(
                                                               "View", 
                                                               "module=files&action=showfiles&obj=" 
                                                                       + crudObject.getName() 
                                                                       + "&row_id=" 
                                                                       + machineDataSet.getInteger(crudTable.getIdFieldName())
                                                                       + "&filesfieldname=" + getFieldKey(), 
                                                               "default", 
                                                               false, 
                                                               "fa fa-files-o",
                                                               "modal-lg"
                                                              );                    

                         Map<Integer,Map<String,Object>> buttonsInTable = new LinkedHashMap<>();
                         Map<String,Object> buttonContainer = new LinkedHashMap<>();
                         buttonsInTable.put(1, button_view);                    
                         buttonContainer.put("buttons", buttonsInTable);

                    
                    columns.put(index,buttonContainer);
                    
                    
                } else if (datatype==DataTypes.TYPE_LOCATION) {
                                     

                        var button_view = new NCodeButtons().createButton(
                                                               "View", 
                                                               "module=location&action=showlocation&obj=" 
                                                                       + crudObject.getName() 
                                                                       + "&row_id=" 
                                                                       + machineDataSet.getInteger(crudTable.getIdFieldName())
                                                                       + "&locationfieldname=" + getFieldKey()
                                                                       + "&countryname=" + getDefaultCountry(), 
                                                               "default", 
                                                               false, 
                                                               "fa fa-location-arrow",
                                                               "modal-lg"
                                                              );                    

                         Map<Integer,Map<String,Object>> buttonsInTable = new LinkedHashMap<>();
                         Map<String,Object> buttonContainer = new LinkedHashMap<>();
                         buttonsInTable.put(1, button_view);                    
                         buttonContainer.put("buttons", buttonsInTable);

                    
                    columns.put(index,buttonContainer);
                    
                    
                } else if (datatype==DataTypes.TYPE_DATE) {
                    if (machineDataSet.getDate(getFieldKey())==null) {
                         columns.put(index,"null");
                    } else {
                         columns.put(index,new SimpleDateFormat(getFormat()).format(machineDataSet.getDate(getFieldKey())));
                    }
                } else if (datatype==DataTypes.TYPE_TIME) {
                    columns.put(index,machineDataSet.getTime(getFieldKey()).toString());
                } else if (datatype==DataTypes.TYPE_DATETIME) {
                    if (machineDataSet.getTimestamp(getFieldKey())==null) {
                        columns.put(index,"null");
                    } else {
                        columns.put(index,new SimpleDateFormat(getFormat()).format(machineDataSet.getTimestamp(getFieldKey()).getTime()));                                            
                    }
                } else if (datatype==DataTypes.TYPE_ENUM) {
                    columns.put(index,machineDataSet.getEnum(getFieldKey()));
                } else {

                }
                
        }
        
        public Map<String,String> getExternal () throws Exception {
        

                                    MachineDataSet machineDataSet = null;
                                    
                                    ObjectLoader loader = new ObjectLoader();
                                    
                                    try {
                                        
                                        
                                        if (getExternalTable()==null) {
                                            setExternalTable(loader.get(getExternalObject()).getDefaultCrudTable().getName() );
                                        }
                                        
                                        if (getExternalJoinField()==null) {
                                            setExternalJoinField(loader.get(getExternalObject()).getDefaultCrudTable().getIdFieldName());
                                        }
                                        
                                        machineDataSet = new DataConnector(getGlobalEnvs(),getUsername())
                                                                            .getTable(getExternalTable())
                                                                            .select()
                                                                                .getColumn(getExternalGetField())
                                                                                .getColumn(getExternalJoinField())
                                                                                .setAddQuery(getExternalAddQuery())
                                                                            .compile()
                                                                            .executeSelect();
                                        
                                    } catch (SQLException | ClassNotFoundException ex) {
                                        throw new Exception("Error on getting external field \"" + getExternalGetField() + "\" from external table \"" + getExternalTable());
                                    }
 
                                    
                                    Map<String,String> externalValues = new LinkedHashMap<>();

                                    CrudObject extObject = loader.get(getExternalObject());
                                    if (extObject == null) {
                                        throw new Exception("Requested external obj \"" + getExternalObject() + "\" is null");
                                    }
                                    
                                    
                                    if (getFieldName()==null) {
                                       throw new Exception("external Field \"" + getFieldName() + "\" is null");
                                    }
                                    
                                    if (getExternalTable()==null) {
                                       throw new Exception("External Table \"" + getExternalTable() + "\" is null");
                                    }
                                    
                                    CrudTable n_externalTable = extObject.getCrudTable(getExternalTable());
                                    
                                    if (n_externalTable==null) {
                                        throw new Exception("External table " + getExternalTable() + " is null in object " + getExternalObject());
                                    }
                                    
                                    int joinFieldDatatype = n_externalTable.getIdFieldDataType();
                                    
                                    int externalFieldDataType = n_externalTable.getField(getExternalGetField()).getDataType();
                                    
                                    
                                    
                                    ////// Add 'NO CHOICE' option if needed /////
                                    
                                    if (isExternalNoChoiceOption()) {
                                        
                                            externalValues.put(String.valueOf(0),"---------");

                                    }                                    
                                    
                                    ///////////////////////////////////////////////////////////////////////////////////
                                    
                                    
                                    
                                    
                                    while (machineDataSet.next()) {
                                        
                                            if (externalFieldDataType==DataTypes.TYPE_STRING) {

                                                    switch (joinFieldDatatype) {
                                                        case DataTypes.TYPE_INT:
                                                            externalValues.put(String.valueOf(machineDataSet.getInteger(getExternalJoinField())),machineDataSet.getString(getExternalGetField()));
                                                            break;
                                                        case DataTypes.TYPE_LONG:
                                                            externalValues.put(String.valueOf(machineDataSet.getLong(getExternalJoinField())),machineDataSet.getString(getExternalGetField()));
                                                            break;
                                                        default:
                                                            externalValues.put(String.valueOf(machineDataSet.getInteger(getExternalJoinField())),machineDataSet.getString(getExternalGetField()));
                                                            break;
                                                    }

                                            } else if (externalFieldDataType==DataTypes.TYPE_INT) {

                                                    switch (joinFieldDatatype) {
                                                        case DataTypes.TYPE_INT:
                                                            externalValues.put(String.valueOf(machineDataSet.getInteger(getExternalJoinField())),String.valueOf(machineDataSet.getInteger(getExternalGetField())));
                                                            break;
                                                        case DataTypes.TYPE_LONG:
                                                            externalValues.put(String.valueOf(machineDataSet.getLong(getExternalJoinField())),String.valueOf(machineDataSet.getInteger(getExternalGetField())));
                                                            break;
                                                        default:
                                                            externalValues.put(String.valueOf(machineDataSet.getInteger(getExternalJoinField())),String.valueOf(machineDataSet.getInteger(getExternalGetField())));
                                                            break;
                                                    } 

                                            } else if (externalFieldDataType==DataTypes.TYPE_DOUBLE) {

                                                    switch (joinFieldDatatype) {
                                                        case DataTypes.TYPE_INT:
                                                            externalValues.put(String.valueOf(machineDataSet.getInteger(getExternalJoinField())),String.valueOf(machineDataSet.getDouble(getExternalGetField())));
                                                            break;
                                                        case DataTypes.TYPE_LONG:
                                                            externalValues.put(String.valueOf(machineDataSet.getLong(getExternalJoinField())),String.valueOf(machineDataSet.getDouble(getExternalGetField())));
                                                            break;
                                                        default:
                                                            externalValues.put(String.valueOf(machineDataSet.getInteger(getExternalJoinField())),String.valueOf(machineDataSet.getDouble(getExternalGetField())));
                                                            break;
                                                    } 

                                            }
                                        
                                    }
                                    
                                    
                                    

                                    
                                    
                                    return externalValues;
        
        
        }
        
}
