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

package org.acceix.frontend.crud;

import org.acceix.frontend.crud.loaders.ObjectLoader;
import org.acceix.frontend.crud.models.CrudField;
import org.acceix.frontend.crud.models.CrudFilterField;
import org.acceix.frontend.crud.models.CrudTable;
import org.acceix.frontend.crud.models.CrudObject;
import org.acceix.frontend.helpers.ModuleHelper;
import org.acceix.frontend.helpers.NCodeButtons;
import org.acceix.ndatabaseclient.DataTypes;
import org.acceix.frontend.helpers.RequestObject;
import org.acceix.frontend.web.commons.DataUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.ndatabaseclient.MachineDataSet;
import org.acceix.ndatabaseclient.DataComparable;
import org.acceix.ndatabaseclient.DataConnector;
import org.acceix.ndatabaseclient.DataDeletable;
import org.acceix.ndatabaseclient.DataInsertable;
import org.acceix.ndatabaseclient.DataSelectable;
import org.json.simple.parser.ParseException;


/**
 *
 * @author zrid
 */
public class ObjectOperations {
    
    ModuleHelper crudModule;
    
    ObjectLoader loader = new ObjectLoader();
    
    DataConnector dataConnector = null;

    public ObjectOperations(ModuleHelper crudModule) {
        this.crudModule = crudModule;
        dataConnector = new DataConnector(this.crudModule.getGlobalEnvs(),true,crudModule.getUsername());
    }
    
    public int createInList (CrudObject crudObject, CrudTable crudTable,Map<String,Object> inputParamsFromClient) throws SQLException, ClassNotFoundException {
        
                final Map<String,Object> inputParamsToBeInserted = new LinkedHashMap<>();
                
                inputParamsFromClient.entrySet()
                                     .stream()
                                     .filter((input) -> !(input.getKey().equals("obj"))).forEachOrdered((input) -> {
                                    inputParamsToBeInserted.put(input.getKey(), input.getValue());
                });
                
                DataInsertable dataInsertable = dataConnector.getTable(crudTable.getName()).insert();  

                    
                DataTypes dataTypes = new DataTypes();
                                
                for (var inputDataMap : inputParamsToBeInserted.entrySet() ) {
                    
                        CrudField crudField = crudTable.getField(inputDataMap.getKey());
                        
                        if (!crudField.isCreatable()) continue;
                                                    
                            dataInsertable = dataInsertable.add(crudField.getFieldName(), 
                                                                dataTypes.convertByDataType(inputDataMap.getValue(),
                                                                                                crudField.getDataType(), 
                                                                                               crudField.getFormat())); 


                }
                
                dataInsertable.setDebug(true);

            return dataInsertable.compile()
                                 .executeAndGetID();                
                
                    
    }
    
    
    
    
    public int prepareForCreate (CrudObject crudObject, CrudTable crudTable,Map<String,Object> inputParamsFromClient) throws SQLException, ClassNotFoundException, Exception {
        
        
                final Map<String,Object> inputParamsToBeInserted = new LinkedHashMap<>();
 
                Map<String,Integer> tables_involved = new LinkedHashMap<>();
                
                for (Map.Entry<String,Object> paramField : inputParamsFromClient.entrySet()) {

                    String paramInputField = paramField.getKey();
                    
                    
                    if (paramInputField.equals("obj")) continue; // bypass object key

                                        
                    CrudField crudField = crudTable.getField(paramInputField);   
                    
                    if (crudField.isExternal()) {
                    
                            if (crudField.getExternalTable()==null) {
                                
                                    CrudObject fieldsExternalObject =  loader.get(crudField.getExternalObject());

                                    if (fieldsExternalObject != null) {

                                            CrudTable fieldsExternalTable = fieldsExternalObject.getDefaultCrudTable();

                                            if (fieldsExternalTable != null) {
                                                crudField.setExternalTable(fieldsExternalTable.getName());
                                            } else {
                                                throw new Exception("Something went wrong with field (External table not found): " + crudField.getFieldKey() + "->" + crudField.getExternalTable());
                                            }
                                    } else {
                                            throw new Exception("Something went wrong with field (External object not found): " + crudField.getFieldKey() + "->" + crudField.getExternalObject());
                                    }
                                    
                            }

                            if (crudField.getExternalJoinField()==null) {
                                
                                crudField.setExternalJoinField(loader.get(crudField.getExternalObject())
                                                                                .getDefaultCrudTable()
                                                                                .getIdFieldName());
                                
                            }   
                    }

                    if (crudField.isExternal() && !crudField.isExternalForCreate()) {
                        

                                    Map<String,Object> external_inputParams = new LinkedHashMap<>();
                                    external_inputParams.put(crudField.getExternalGetField(), (String)inputParamsFromClient.get(paramInputField));

                                    int autoGeneratedId,effected_rows;

                                    if (tables_involved.get(crudField.getExternalTable()) == null) { 
                                            autoGeneratedId = createInDatabase(loader.get(crudField.getExternalObject())
                                                                                            .getCrudTable(crudField.getExternalTable()),
                                                                                            external_inputParams);

                                            tables_involved.put(crudField.getExternalTable(),autoGeneratedId);
                                            
                                            if ( autoGeneratedId < 0 ) return -1; 
                                            
                                    } else { // if already inserted to same external table , then update date not insert

                                            effected_rows = updateInDatabaseAndGetId(crudField.getExternalObject(), 
                                                                                          crudField.getExternalTable(), 
                                                                                          external_inputParams,
                                                                                          tables_involved.get(crudField.getExternalTable()));

                                            if (  effected_rows < 1 ) return -1;                                    
                                    }

                                    inputParamsToBeInserted.put(paramInputField,
                                                                String.valueOf(tables_involved.get(crudField.getExternalTable())));
                                

                   } else {
                        inputParamsToBeInserted.put(paramInputField, inputParamsFromClient.get(paramInputField));
                   }
                    
                }
                
                return createInDatabase(crudTable, inputParamsToBeInserted);        
    }
    
    
    
    public int createInDatabase(CrudTable t_crudTable,Map<String,Object> mapToAppend) throws SQLException, ClassNotFoundException {
        

                DataInsertable dataInsertable = dataConnector.getTable(t_crudTable.getName())
                                                                    .insert();  


                
                    List<String> fieldsOfTable = new LinkedList(t_crudTable.getFieldList());

                    fieldsOfTable.removeAll(new LinkedList<>(mapToAppend.keySet()));

                    fieldsOfTable.forEach((var fieldOfTable) -> {
                            if (t_crudTable.getField(fieldOfTable).getDefaultValue() != null) {
                                    mapToAppend.put(fieldOfTable, t_crudTable.getField(fieldOfTable).getDefaultValue());
                            }
                    });
                    
                DataTypes dataTypes = new DataTypes();

                                
                for (var inputDataMap : mapToAppend.entrySet() ) {
                    

                        CrudField crudField = t_crudTable.getField(inputDataMap.getKey());

                        if (!crudField.isCreatable() && crudField.getDefaultValue() == null) continue;
                        
                        if (inputDataMap.getValue()==null || String.valueOf(inputDataMap.getValue()).isEmpty()) { continue; };

                        
                        
                        
                            if (crudField.getDefaultValue() != null && crudField.getDefaultValue().equals("$user_id$")) {
                                dataInsertable = dataInsertable.add(crudField.getFieldName(), 
                                                                dataTypes.convertByDataType(String.valueOf(crudModule.getUserId()),
                                                                                                DataTypes.TYPE_INT, 
                                                                                               crudField.getFormat())); 
                            } else {                       
                                
                                if (!crudField.isCrypted()) {

                                    dataInsertable = dataInsertable.add(crudField.getFieldName(), 
                                                                    dataTypes.convertByDataType(inputDataMap.getValue(),
                                                                                                    crudField.getDataType(), 
                                                                                                   crudField.getFormat()));
                                    
                                } else {
                                    
                                    MessageDigest md5 = null;
                                    try {
                                        md5 = MessageDigest.getInstance("MD5"); // you can change it to SHA1 if needed!
                                    } catch (NoSuchAlgorithmException ex) {
                                        Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    md5.update(inputDataMap.getValue().toString().getBytes(), 0, inputDataMap.getValue().toString().length());

                                    String crypted = new BigInteger(1, md5.digest()).toString(16);   
                                    
                                    dataInsertable = dataInsertable.add(crudField.getFieldName(), 
                                                                    dataTypes.convertByDataType(crypted,
                                                                                                    crudField.getDataType(), 
                                                                                                   crudField.getFormat()));                                    
                                    
                                }
                            }

                }
                
                dataInsertable.setDebug(true);

            return dataInsertable.compile()
                                 .executeAndGetID();


        

    }
    
    public int deleteElementFromDatabase(CrudTable crudtable,int row_id) {
        
            String idFied;
            if (crudtable.getListIdField().isEmpty()) {
                idFied = crudtable.getIdFieldName();
            } else {
                idFied = crudtable.getListIdField();
            }
            

            try {
                DataDeletable dataDeletable = dataConnector.getTable(crudtable.getName())
                                                                .delete();
                        dataDeletable.setDebug(true);
                        dataDeletable.where()
                            .eq(idFied, row_id)
                        .compile()
                        .execute();
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
                                        

        return 1;
    }
    
    public int deleteListElementFromDatabase(CrudTable crudtable,int row_id) throws ClassNotFoundException, SQLException {
        
            String idField = crudtable.getIdFieldName();


                new DataConnector(this.crudModule.getGlobalEnvs(),true,crudModule.getUsername())
                        .getTable(crudtable.getName())
                        .delete()
                        .where()
                            .eq(idField, row_id)
                        .compile()
                        .execute();

                                        

        return 1;
    }   

    public int updateInDatabaseAndGetId(String crudobject,String t_crudtable,Map<String,Object> mapToAppend,int row_id) {
        
        
                var crudTable = loader.get(crudobject).getCrudTable(t_crudtable);
        
                var dataUpdateable = dataConnector.getTable(crudTable.getName()).update();  
                
                var dataTypes = new DataTypes();


                    for (Map.Entry<String,Object> inputDataMap : mapToAppend.entrySet()) {

                            var crudField = crudTable.getField(inputDataMap.getKey());
                            
                            if (crudField==null) { System.out.println("field: " + inputDataMap.getKey() + " is null in table " + t_crudtable + " = " + crudTable.getName()); }

                            if ( String.valueOf(inputDataMap.getValue()).equals("") ) continue;
                            
                            if (crudField.isCrypted()) {
                                
                                MessageDigest md5 = null;
                                try {
                                    md5 = MessageDigest.getInstance("MD5"); // you can change it to SHA1 if needed!
                                } catch (NoSuchAlgorithmException ex) {
                                    Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                md5.update(inputDataMap.getValue().toString().getBytes(), 0, inputDataMap.getValue().toString().length());
                                
                                String crypted = new BigInteger(1, md5.digest()).toString(16);
                                
                                dataUpdateable = dataUpdateable.update(inputDataMap.getKey(), dataTypes.convertByDataType(crypted, crudField.getDataType(), crudField.getFormat()));                                
                                
                                
                            } else {

                                dataUpdateable = dataUpdateable.update(inputDataMap.getKey(), dataTypes.convertByDataType(inputDataMap.getValue(), crudField.getDataType(), crudField.getFormat()));
                                
                            }

                    }


                    try {
                        dataUpdateable.setDebug(true);
                            return dataUpdateable.where()
                                                    .eq(crudTable.getIdFieldName(), row_id)
                                                 .compile()
                                                 .execute();
                    } catch (SQLException | ClassNotFoundException ex) {
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        return -1;
                    } 
        

    }
    

    public Map<Integer,Map<Object,Object>> getListDataFromDatabase(CrudObject crudobject,CrudTable crudtable,String idFieldName,int id,RequestObject requestObject) throws Exception {


        
                    DataSelectable selectHandler = dataConnector.getTable(crudtable.getName()).select(); 

                    for (CrudField crudField : crudtable.getFields()) {
                            if (crudField.isListData() && !crudField.getFieldName().equals(crudtable.getIdFieldName())) {
                                
                            }
                            if (crudField.isExternal()) {
                                
                                    String externalTable = crudField.getExternalTable();
                                    String externalJoinField = crudField.getExternalJoinField();

                                    if (externalTable==null) {
                                        crudField.setExternalTable(loader.get(crudField.getExternalObject())
                                                                                .getDefaultCrudTable()
                                                                                .getName());
                                    }

                                    if (externalJoinField==null) {
                                        crudField.setExternalJoinField(loader.get(crudField.getExternalObject())
                                                                                        .getDefaultCrudTable()
                                                                                        .getIdFieldName());
                                    }                

                                    
                                selectHandler = selectHandler.joinTable(crudField.getExternalTable(),crudField.getFieldName(),crudField.getExternalJoinField())
                                                                .getColumnAs(crudField.getExternalGetField(),crudField.getFieldKey())
                                                             .endJoin();
                                    
                            } else {
                                selectHandler = selectHandler.getColumn(crudField.getFieldKey());
                            }
                    }

                    Map<Integer,Map<Object,Object>> rows = new LinkedHashMap<>();

                
                    try {                       

                            //////////////////////// APPLY ID FIELD AND LIST ID FIELD ////////////////////////////
                            selectHandler = selectHandler.getColumn(crudtable.getIdFieldName())
                                                         .getColumn(crudtable.getListIdField());
                            //////////////////////////////////////////////////////////////////////////////////////

                            
                            //////////////////////////////// APPLY ADD QUERY /////////////////////////////////////
                            if (crudtable.getAddQuery()!=null)
                                selectHandler.setAddQuery(crudtable.getAddQuery());
                            //////////////////////////////////////////////////////////////////////////////////////
                            
                            DataComparable nDataComparable = selectHandler.where();

                            ///////////////////////////////// ID FIELD TO WHERE CONDITION APPLY /////////////////////////////////////
                            
                            if (id > -1) nDataComparable.eq(idFieldName, id);

                            /////////////////////////////////// READ FILTER APPLY ///////////////////////////////
                                crudModule.addToDataModel("filters",applyFiltersOnRead(crudobject, crudtable, nDataComparable, requestObject,dataConnector));
                            

                            //////////////////////// GROUP by LIST id if object is LIST //////////////////////////
                            if (!crudtable.getListIdField().isEmpty() && id < 0) 
                                nDataComparable = nDataComparable.groupBy(crudtable.getListIdField());


                      
                      
                      
                      //////////////////////////////////// ORDER BY FIELDS APPLY ////////////////////////////////////////
                      
                      if (crudtable.getOrderByFields().size() > 0) {
                          if (crudtable.getOrderByDirection()==CrudTable.ORDER_BY_DESC) {
                              
                                for (String column : crudtable.getOrderByFields()) {
                                    nDataComparable  = nDataComparable.orderByDesc(column);
                                }

                          } else {
                              
                                for (String column : crudtable.getOrderByFields()) {
                                    nDataComparable  = nDataComparable.orderByAsc(column);
                                }                              
                            
                          }
                      }

                      
                      
                      //////////////////////////////////// READ LIMIT APPLY //////////////////////////////////
                      
                      if (crudtable.getReadLimit().length() > 0) {
                          nDataComparable  = nDataComparable.setLimit(crudtable.getReadLimit());
                      }
                      
                      
                      ///////////////////////////////////// EXECUTE QUERY ////////////////////////////////////
                      MachineDataSet machineDataSet = nDataComparable.compile()
                                                       .executeSelect();                        

                      
                        int index = 0;
                        
                        while (machineDataSet.next()) {
                            
                            Map<Object,Object> columns = new LinkedHashMap<>();

                            ////////////////////////////// Apply ID field to data model //////////////////////////////////

                            CrudField idField = new CrudField(crudtable.getIdFieldName(),crudtable.getIdFieldName(),crudtable.getIdFieldDataType());

                            idField.crudfieldToColumn(columns, 
                                                machineDataSet,
                                                crudobject,
                                                crudtable);
                            
                            /////////////////////////////////////////////////////////////////////////////////////////////

                            
                            for (CrudField crudfield : crudtable.getFields()) {
                                
                               if (!crudfield.isListData())  continue;
                                        
                                    if (!crudtable.getListIdField().equals(crudfield.getFieldName())) {

                                         crudfield.crudfieldToColumn(columns, 
                                                            machineDataSet,
                                                            crudobject,
                                                            crudtable);
                                    }

                               
                            } 
                            
                            
                            ///////////////////////////////////////// ADD EDIT BUTTONS //////////////////////////////////////////
                            if (crudobject.isEditable()) {
                                
                                    Map<Integer,Map<String,Object>> buttonsInTable = new LinkedHashMap<>();
                                    Map<String,Object> buttonContainer = new LinkedHashMap<>();


                                        Map<String,Object> button_edit = new NCodeButtons().createButton("Remove", 
                                                                          "module=crud&action=removeListElement&obj="
                                                                                  + crudobject.getName() 
                                                                                  + "&list_id=" 
                                                                                  + machineDataSet.getInteger(crudtable.getListIdField())                                                                                  
                                                                                  + "&row_id=" 
                                                                                  + machineDataSet.getInteger(crudtable.getIdFieldName()), 
                                                                          "red", 
                                                                          true, 
                                                                          "fa fa-check-circle", 
                                                                          "modal-xl");   


                                    buttonsInTable.put(1, button_edit);
                                    
                                    buttonContainer.put("buttons", buttonsInTable);   

                                    columns.put(String.valueOf(columns.size()+1),buttonContainer);
                                
                            }
                            
                            
                            
                            rows.put(index, columns);
                            index++;
                        }
                        
                        dataConnector.closeConnection();
                        return rows;
                    } catch (SQLException | ClassNotFoundException ex) {
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }
                

        
    }
    
    
    
    public Map<Integer,Map<Object,Object>> getFileListFromDatabase(CrudObject crudObject,CrudTable crudTable,String idFieldName,int id,RequestObject requestObject) throws ClassNotFoundException, SQLException, IOException, ParseException {
        
        Map<Integer,Map<Object,Object>> rows = new LinkedHashMap<>();
        
        var dataSet_files = dataConnector.getTable(crudTable.getName())
                                            .select()
                                                .getColumn((String)requestObject.getParams().get("filesfieldname"))
                                            .where()
                                                .eq(idFieldName, id)
                                            .compile()
                                            .executeSelect();
        
            String files;
        
            if (dataSet_files.next()) {
                files = dataSet_files.getString((String)requestObject.getParams().get("filesfieldname"));  
                if (files==null) return rows;
            } else {
                return rows;
            }

            
            var filesList = (List)new DataUtils().readJsonArrayFromString(files);
            
           

            int index=1;

                for (Object item_of_list : filesList) {
                    

                    var dataSet_file_data = new DataConnector(this.crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                            .getTable("npt_files")
                                                            .select()
                                                                .getColumn("id")
                                                                .getColumn("orig_name")
                                                                .getColumn("file_size")
                                                            .where()
                                                                .eq("id", Integer.parseInt(item_of_list.toString()))
                                                            .compile()
                                                            .executeSelect();   

                    if (dataSet_file_data.next()) {
                        
                            String orig_name = dataSet_file_data.getString("orig_name");
                            String file_size = String.valueOf(dataSet_file_data.getLong("file_size"));
                            String file_id = String.valueOf(dataSet_file_data.getInteger("id"));
                                                       
                            
                            var columns = new LinkedHashMap<>();
                            columns.put(columns.size(), index);
                            columns.put(columns.size(), orig_name);
                            columns.put(columns.size(), file_size);
                                                      
                            
                            
                            var button_view = new NCodeButtons().createLink(
                                                                   "View", 
                                                                   "module=files&action=viewfile&obj=" 
                                                                           + crudObject.getName() 
                                                                           + "&file_id=" + file_id
                                                                           + "&filesfieldname=" + (String)requestObject.getParams().get("filesfieldname"), 
                                                                   "default", 
                                                                   false, 
                                                                   "fa fa-files-o",
                                                                   "modal-md"
                                                                  );     
                            Map<Integer,Map<String,Object>> linksInTable = new LinkedHashMap<>();
                            Map<String,Object> linksContainer = new LinkedHashMap<>();
                            linksInTable.put(1, button_view);                             
                            linksContainer.put("links", linksInTable);
                            
                            var button_delete = new NCodeButtons().createButton(
                                                                   "Delete", 
                                                                   "module=files&action=deletefile&obj=" 
                                                                           + crudObject.getName() 
                                                                           + "&row_id=" + id                                                                           
                                                                           + "&file_id=" + file_id
                                                                           + "&filesfieldname=" + (String)requestObject.getParams().get("filesfieldname"), 
                                                                   "default", 
                                                                   false, 
                                                                   "fa fa-files-o",
                                                                   "modal-md"
                                                                  );     
                            Map<Integer,Map<String,Object>> buttonsInTable = new LinkedHashMap<>();
                            Map<String,Object> buttonsContainer = new LinkedHashMap<>();
                            buttonsInTable.put(1, button_delete);                             
                            buttonsContainer.put("buttons", buttonsInTable);                              
                           
                            columns.put(columns.size(), linksContainer);
                            columns.put(columns.size(), buttonsContainer);
                            
                            
                            rows.put(rows.size(), columns);
                            index++;
                    }

                }

        return rows;
        
    }
    
    public Map<String, Object> getLocationFromDatabase(CrudObject crudObject,CrudTable crudTable,String idFieldName,int id,RequestObject requestObject) throws ClassNotFoundException, SQLException, IOException, ParseException {
        
        
        final Map<String, Object> fieldsMap = new LinkedHashMap<>();

        
        var dataSet_files = dataConnector.getTable(crudTable.getName())
                                            .select()
                                                .getColumn((String)requestObject.getParams().get("locationfieldname"))
                                            .where()
                                                .eq(idFieldName, id)
                                            .compile()
                                            .executeSelect();
        
                        String files;

                        if (dataSet_files.next()) {
                            files = dataSet_files.getString((String)requestObject.getParams().get("locationfieldname")); 
                            if (files != null) {
                                Map locationInfo = new DataUtils().readJsonObjectFromString(files);
                            }

                        } else {

                        }

            
                   DataConnector dataConnector = new DataConnector(this.crudModule.getGlobalEnvs(),true,crudModule.getUsername());
                   


                        CrudField crudField = crudTable.getField((String)requestObject.getParams().get("locationfieldname"));
                        
                        System.out.println("Fieldname:" + crudField.getDefaultCountry() + " fff: " + requestObject.getParams().get("locationfieldname"));

                        
                    Map<String,Object> mapField = new LinkedHashMap<>();    
                        
                        mapField.put("apikey",crudField.getApiKey());
                        mapField.put("defaultcountry",crudField.getDefaultCountry());
                        mapField.put("defaultcity",crudField.getDefaultCity());
                        
                    
                    fieldsMap.put("mapfield", mapField);                       
                        
                    Map<String,Object> addressField = new LinkedHashMap<>();

                        addressField.put("displayname", "Address");
                        addressField.put("name", "address");
                        addressField.put("datatype", DataTypes.TYPE_STRING);
                        addressField.put("values", "");

                    fieldsMap.put("address", addressField);
                    
                    MachineDataSet machineDataSetOfCountryId = dataConnector.getTable("npt_countries")
                                                                            .select()
                                                                                .getColumn("id")
                                                                            .where()
                                                                                .eq("nicename",(String)requestObject.getParams().get("countryname"))
                                                                            .compile()
                                                                            .executeSelect();                
                    
                    MachineDataSet machineDataSetOfCityList = dataConnector.getTable("npt_cities")
                                                                            .select()
                                                                                .getColumn("id")
                                                                                .getColumn("name")
                                                                            .where()
                                                                                .eq("country_id", machineDataSetOfCountryId.getFirstInt("id"))
                                                                            .compile()
                                                                            .executeSelect();

                        Map<String,String> cityListFromDb = new LinkedHashMap<>();

                        while (machineDataSetOfCityList.next()) {
                              cityListFromDb.put(String.valueOf(machineDataSetOfCityList.getInteger("id")),machineDataSetOfCityList.getString("nicename"));
                        }                     

                    Map<String,Object> cityField = new LinkedHashMap<>();

                        cityField.put("displayname", "City");
                        cityField.put("name", "city");
                        cityField.put("datatype", "external");
                        cityField.put("values", cityListFromDb);
                        cityField.put("selected", crudField.getDefaultCity());
                        
                    fieldsMap.put("cities", cityField); 
                    
                    
                    // Add country list
                    MachineDataSet machineDataSetOfCountryList = dataConnector.getTable("npt_countries")
                                .select()
                                    .getColumn("id")
                                    .getColumn("nicename")
                                .compile()
                                .executeSelect();

                        Map<String,String> countryListFromDb = new LinkedHashMap<>();

                        while (machineDataSetOfCountryList.next()) {
                              countryListFromDb.put(String.valueOf(machineDataSetOfCountryList.getInteger("id")),
                                                                    machineDataSetOfCountryList.getString("nicename"));
                        }                    

                    Map<String,Object> countryField = new LinkedHashMap<>();

                        countryField.put("displayname", "Country");
                        countryField.put("name", "country");
                        countryField.put("datatype", "external");
                        countryField.put("values", countryListFromDb);
                        countryField.put("selected", crudField.getDefaultCountry());
                        
                    fieldsMap.put("countries", countryField);
                    /////
                    
                    Map<String,Object> fullAddressField = new LinkedHashMap<>();

                        fullAddressField.put("displayname", "Full address");
                        fullAddressField.put("name", "full_address");
                        fullAddressField.put("datatype", "string");
                        fullAddressField.put("values", "sfsfsfsfsffs fsdfsfsd 45/6");
                        
                    fieldsMap.put("full_address", fullAddressField);
                    /////  
                    
                    Map<String,Object> area = new LinkedHashMap<>();
                        area.put("displayname", "Area");
                        area.put("name", "area");
                        area.put("datatype", "string");
                        area.put("values", "erwrwerwer");
                        
                    fieldsMap.put("area", area);
                    ///// 
                    
                    Map<String,Object> latitute = new LinkedHashMap<>();
                        latitute.put("name", "latitute");
                        latitute.put("datatype", "hidden");
                        latitute.put("values", "0");
                    fieldsMap.put("latitute", latitute);
                    /////  

                    Map<String,Object> longitute = new LinkedHashMap<>();
                        longitute.put("name", "longitute");
                        longitute.put("datatype", "hidden");
                        longitute.put("values", "0");
                    fieldsMap.put("longitute", longitute);
                    /////                      
                    
            

        return fieldsMap;
        
    }
    
    
    public Map<Integer,Map<Object,Object>> readDataFromDb(CrudObject crudobject,CrudTable crudtable,int id,RequestObject requestObject) throws ClassNotFoundException, SQLException, Exception {


                    DataSelectable selectHandler = dataConnector.getTable(crudtable.getName()).select(); 

                    for (CrudField crudField : crudtable.getFields()) {

                            if (!crudField.isViewable() && !crudtable.getListIdField().equals(crudField.getFieldKey())) continue;
                            
                            if (crudField.getFieldKey().equals(crudtable.getIdFieldName())) continue;
                            
                            if (crudField.isListData()) continue; // Ignore list data fields of table

                            if (crudField.isExternal()) { // External field


                                    if (crudField.getExternalTable()==null) {
                                            CrudObject fieldsExternalObject =  loader.get(crudField.getExternalObject());

                                            if (fieldsExternalObject != null) {

                                                    CrudTable fieldsExternalTable = fieldsExternalObject.getDefaultCrudTable();

                                                    if (fieldsExternalTable != null) {
                                                        crudField.setExternalTable(fieldsExternalTable.getName());
                                                    } else {
                                                        throw new Exception("Something went wrong with field (External table not found): " + crudField.getFieldKey());
                                                    }
                                            } else {
                                                    throw new Exception("Something went wrong with field (External object not found): " + crudField.getFieldKey()  + "->" + crudField.getExternalObject());
                                            }
                                    }

                                    if (crudField.getExternalJoinField()==null) {
                                        crudField.setExternalJoinField(loader.get(crudField.getExternalObject())
                                                                                        .getDefaultCrudTable()
                                                                                        .getIdFieldName());
                                    }                
                                    
                                selectHandler = selectHandler.joinTable(crudField.getExternalTable(),crudField.getFieldName(),crudField.getExternalJoinField())
                                                                .getColumnAs(crudField.getExternalGetField(),crudField.getFieldKey())
                                                             .endJoin();
                                    
                            } else if (crudField.getStatementRead() != null) { // Column as statement
                                
                                selectHandler = selectHandler.getColumnStatement(crudField.getStatementRead(),crudField.getFieldKey());
                                
                            } else { //////// Regular column
                                
                                selectHandler = selectHandler.getColumn(crudField.getFieldKey());
                                
                            }

                    }
                    
                    selectHandler.setDebug(true);
                    
                    Map<Integer,Map<Object,Object>> rows = new LinkedHashMap<>();
                      

                            ///////////////////////////////////////// ID FIELD APPLY //////////////////////////////////////////
                            selectHandler = selectHandler.getColumn(crudtable.getIdFieldName());
                            

                            //////////////////////// LIST ID FIELD APPLY IF TABLE IS LIST /////////////////////////////////////
                            if (!crudtable.getListIdField().isEmpty()) {
                                selectHandler = selectHandler.getColumn(crudtable.getListIdField());
                            }

                            //////////////////////////////// APPLY ADD QUERY /////////////////////////////////////
                            if (crudtable.getAddQuery()!=null) selectHandler.setAddQuery(crudtable.getAddQuery());

                            
                            DataComparable nDataComparable = selectHandler.where();
                            
                            ///////////////////////////////// FILTER BY ID FIELD IF WE HAVE IT /////////////////////////////////////
                            if (id > -1) nDataComparable.eq(crudtable.getIdFieldName(), id);

                            ///////////////////////////////////////// READ FILTER APPLY ///////////////////////////////////////////// 
                            crudModule.addToDataModel("filters",applyFiltersOnRead(crudobject, crudtable, nDataComparable, requestObject,dataConnector));


                            //////////////////////// GROUP by LIST id if object is LIST //////////////////////////
                            if (!crudtable.getListIdField().isEmpty() && id < 0) {
                                nDataComparable = nDataComparable.groupBy(crudtable.getName() + "." + crudtable.getListIdField());
                            }

                      
                      
                      //////////////////////////////////// ORDER BY FIELDS APPLY ////////////////////////////////////////
                      
                      if (crudtable.getOrderByFields().size() > 0) {
                          
                          if (crudtable.getOrderByDirection()==CrudTable.ORDER_BY_DESC) {
                                for (String column : crudtable.getOrderByFields()) {
                                    nDataComparable  = nDataComparable.orderByDesc(column);
                                }
                          } else {   
                                for (String column : crudtable.getOrderByFields()) {
                                    nDataComparable  = nDataComparable.orderByAsc(column);
                                }                              
                          }
                          
                      }
                      
                      //////////////////////////////////////////////////////////////////////////////////////////////////

                      
                      
                      ///////////////////////////////////////// READ LIMIT APPLY ///////////////////////////////////////
                      
                      if (crudtable.getReadLimit().length() > 0) {
                          nDataComparable  = nDataComparable.setLimit(crudtable.getReadLimit());
                      }
                      
                      
                      ////////////////////////////////// EXECUTE QUERY /////////////////////////////////
                      var machineDataSet = nDataComparable.compile()
                                                          .executeSelect();                        

                      
                        int index = 0;
                        while (machineDataSet.next()) {
                            
                           var columns = new LinkedHashMap<>();

                            ////////////////////////////// Apply ID field to data model //////////////////////////////////
                            CrudField crudField = new CrudField(crudtable.getIdFieldName(),crudtable.getIdFieldName(),crudtable.getIdFieldDataType());
                            crudField.crudfieldToColumn(columns, 
                                                            machineDataSet,
                                                            crudobject,
                                                            crudtable);
                            
                            /////////////////////////////////////////////////////////////////////////////////////////////

                            for (CrudField crudfield : crudtable.getFields()) {
                                
                                    if (!crudfield.isViewable() && !crudtable.getListIdField().equals(crudfield.getFieldName())) { continue; }
    
                                    if (!crudtable.getListIdField().equals(crudfield.getFieldKey())) {

                                        if (crudfield.isListData()) continue;
                                        
                                        //System.out.println("Geting field3: " + crudfield.getFieldKey() + " " + crudfield.getFieldKey());
                                        crudfield.crudfieldToColumn(columns, 
                                                                    machineDataSet,
                                                                    crudobject,
                                                                    crudtable);

                                    }
                               
                            } 
                            
                            var buttons = new NCodeButtons();
                            
                            //////////////////////////////////////////// ADD EDIT BUTTONS /////////////////////////////////////////////////
                            if (crudobject.isEditable()) {
                                
                                    Map<Integer,Map<String,Object>> buttonsInTable = new LinkedHashMap<>();
                                    Map<String,Object> buttonContainer = new LinkedHashMap<>();

                                    Map<String,Object> button_edit,button_edit_data;
                                    if (!crudtable.getListIdField().isEmpty()) {
                                        
                                        button_edit = buttons.createButton("Edit", 
                                                                          "module=crud&action=getlistupdatemodel&obj="
                                                                                  + crudobject.getName() 
                                                                                  + "&row_id=" 
                                                                                  + machineDataSet.getInteger(crudtable.getListIdField()), 
                                                                          "orange", 
                                                                          false, 
                                                                          "fa fa-pencil", 
                                                                          "modal-lg");

                                        button_edit_data = buttons.createButton("Edit list", 
                                                                          "module=crud&action=readlist&obj="
                                                                                  + crudobject.getName() 
                                                                                  + "&row_id=" 
                                                                                  + machineDataSet.getInteger(crudtable.getListIdField()), 
                                                                          "info", 
                                                                          false, 
                                                                          "fa fa-pencil", 
                                                                          "modal-xl");  
                                        

                                        buttonsInTable.put(1, button_edit);
                                        buttonsInTable.put(2, button_edit_data);
                                        
                                    } else {
                                        
                                        long id_of_field=0;
                                        
                                        if (crudtable.getIdFieldDataType()==DataTypes.TYPE_LONG) {
                                            id_of_field = machineDataSet.getLong(crudtable.getIdFieldName());
                                        } else if (crudtable.getIdFieldDataType()==DataTypes.TYPE_INT) {
                                            id_of_field = (long)machineDataSet.getInteger(crudtable.getIdFieldName());
                                        }
                                        
                                        button_edit = buttons.createButton("Edit", 
                                                                          "module=crud&action=getupdatemodel&obj=" 
                                                                                  + crudobject.getName() 
                                                                                  + "&row_id=" 
                                                                                  + id_of_field, 
                                                                          "orange", 
                                                                          false, 
                                                                          "fa fa-pencil", 
                                                                          "modal-lg");
                                        buttonsInTable.put(1, button_edit);
                                        
                                    }
                                    
                                    buttonContainer.put("buttons", buttonsInTable);   

                                    columns.put(String.valueOf(columns.size()+1),buttonContainer);
                                
                            }
                            
                            
                            
                            rows.put(index, columns);
                            index++;
                        }
                        
                        dataConnector.closeConnection();
                        return rows;

                

        
    }
     
public String getExternalFieldValueById (CrudField crudfield,int value,DataConnector dataConnector) throws ClassNotFoundException, SQLException {

                            String externalTable = crudfield.getExternalTable();
                            String externalJoinField = crudfield.getExternalJoinField();

                            if (externalTable==null) {
                                crudfield.setExternalTable(loader.get(crudfield.getExternalObject())
                                                                            .getDefaultCrudTable().getName() );
                            }

                            if (externalJoinField==null) {
                                crudfield.setExternalJoinField(loader.get(crudfield.getExternalObject())
                                                                                .getDefaultCrudTable().getIdFieldName());
                            }                
        
        
                            return dataConnector.getTable(crudfield.getExternalTable())
                                                .select()
                                                    .getColumn(crudfield.getExternalGetField())
                                                .where()
                                                    .eq(crudfield.getExternalJoinField(), value)
                                                .compile()
                                                .executeSelect()
                                                .getFirstString(crudfield.getExternalGetField());     
                
    }
    
    public Map<Integer,Object> applyFiltersOnRead(CrudObject crudobject,CrudTable crudtable,DataComparable nDataComparable,RequestObject requestObject,DataConnector dataConnector) throws ClassNotFoundException, SQLException {

                            var dataTypes = new DataTypes();
                            
                            Map<Integer,Object> filtersApplied = new LinkedHashMap<>();
        
                            for (CrudFilterField nFilterField : crudtable.getReadFilterFields()) {
                                //System.out.println("Filter: " + nFilterField.getFilterId() + " " + nFilterField.getFieldName());
                                Object fieldValueFromUser = requestObject.getParams().get(nFilterField.getFieldName());
                                Object tempFieldValue = nFilterField.getFieldValue();


                                if ( (fieldValueFromUser!=null)  
                                    && (!fieldValueFromUser.equals("")) 
                                        && (!nFilterField.getType().equals("fixed")) ) {

                                             tempFieldValue = fieldValueFromUser;
                                             Map<String,String> my_filter= new LinkedHashMap<>();
                                             my_filter.put("name", crudtable.getField(nFilterField.getFieldName()).getDisplayName());

                                             if (crudtable.getField(nFilterField.getFieldName()).isExternal()) {

                                                 my_filter.put("value",
                                                         getExternalFieldValueById(crudtable.getField(nFilterField.getFieldName()),
                                                                        Integer.parseInt((String)tempFieldValue),
                                                                        dataConnector
                                                                                   )
                                                                        
                                                 );    

                                             } else {
                                                 my_filter.put("value",(String)tempFieldValue);                                                     
                                             }


                                             filtersApplied.put(filtersApplied.size(), my_filter);
                                                if (crudtable.getField(nFilterField.getFieldName()).getDataType()==DataTypes.TYPE_DATE) {
                                                    tempFieldValue = dataTypes.convertByDataType(tempFieldValue, 
                                                                                                crudtable.getField(nFilterField.getFieldName()).getDataType(), 
                                                                                                crudtable.getField(nFilterField.getFieldName()).getFormat());
                                                }                                                

                                } else {
                                    
                                    if (tempFieldValue instanceof String ) {
                                        if (((String) tempFieldValue).contains("$user_id$")) {
                                            tempFieldValue = (int)crudobject.getUser_id();
                                        } else if (((String) tempFieldValue).contains("$domain$")) {
                                            tempFieldValue = crudobject.getDomain();
                                        } else if  ( (((String) tempFieldValue).contains("$currect_datetime$")) 
                                                            || ( ((String)tempFieldValue).contains("$currect_timestamp$")) ) {
                                            //tempFieldValue = 
                                        }
                                    }

                                }
                                    
                                    
                                    

                                if (tempFieldValue!=null) {
                                    

                                    switch (nFilterField.getCondition()) {
                                        case "eq":
                                            nDataComparable = nDataComparable.eq(nFilterField.getFieldName(), tempFieldValue);
                                            break;
                                        case "ne":
                                            nDataComparable = nDataComparable.ne(nFilterField.getFieldName(), tempFieldValue);
                                            break;
                                        case "gt":
                                            nDataComparable = nDataComparable.gt(nFilterField.getFieldName(), tempFieldValue);
                                            break;
                                        case "lt":
                                            nDataComparable = nDataComparable.lt(nFilterField.getFieldName(), tempFieldValue);
                                            break;
                                        case "ge":
                                            nDataComparable = nDataComparable.ge(nFilterField.getFieldName(), tempFieldValue);
                                            break;
                                        case "le":
                                            nDataComparable = nDataComparable.le(nFilterField.getFieldName(), tempFieldValue);
                                            break;
                                        default:
                                            break;
                                    }

                                }
 
                            }
                            return filtersApplied;
        
    }
    

       
    
    
    public Map<Integer, Object> getCreateModel(String t_crudobject,String t_crudtable) throws Exception {
                
                final Map<Integer, Object> fieldsMap = new LinkedHashMap<>();
               
                for (CrudField field : loader.get(t_crudobject)
                              .getCrudTable(t_crudtable)
                              .getFields() ) {
                                  
                                  
                                    if (!field.isCreatable() || field.isListData()) continue; // Do not continue                                


                                    Map<String,Object> fieldOptions = new LinkedHashMap<>();

                                    fieldOptions.put("displayname", field.getDisplayName());

                                    if (field.isExternal() && field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values",field.getExternal());
                                            fieldOptions.put("selected", field.getDefaultValue());

                                    } else if (field.isExternal() && !field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", field.getExternalFieldDataTypeAsString());

                                    } else {
                                        
                                        if (field.getStatementCreate() == null) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", field.getDataTypeAsString());

                                            if (field.getDataType()==DataTypes.TYPE_ENUM) { 
                                                fieldOptions.put("values",field.getEnumValues()); 
                                            }
                                            
                                        } else {
                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values",getCreateStatementValues(field));
                                        }

                                    }                        
                                    fieldsMap.put(fieldsMap.size(), fieldOptions);
                        
                        
                }
                
                return fieldsMap;
        
    }
    
    public Map<String, Object> getRowDataFromDatabaseById(CrudTable t_crudtable,String fieldName,String idFieldName,int row_id) throws ClassNotFoundException, SQLException {
        return getRowDataFromDatabaseById(t_crudtable, fieldName, idFieldName,row_id,null);
    }
    
    public Map<String, Object> getRowDataFromDatabaseById(CrudTable t_crudtable,String idFieldName,int row_id) throws ClassNotFoundException, SQLException {
        return getRowDataFromDatabaseById(t_crudtable, null,idFieldName,row_id,null);
    }

    private Map<String, Object> getRowDataFromDatabaseById(CrudTable t_crudtable,String getFieldName,String idFieldName,int row_id,String addQuery) throws ClassNotFoundException, SQLException {

                
                Map<String,Object> rowData = new LinkedHashMap<>();
                
                DataSelectable nDataSelectable = dataConnector.getTable(t_crudtable.getName())
                                                               .select();
                
                Collection<CrudField> fields;
                
                if (getFieldName != null) {
                    
                        CrudField singleField = t_crudtable.getField(getFieldName);
                        fields = new ArrayList<>();
                        fields.add(singleField);

                } else {
                    
                        fields = t_crudtable.getFields();
                    
                }

                   

                for (CrudField field : fields) {
                    nDataSelectable = nDataSelectable.getColumnAs(field.getFieldName(),field.getFieldKey());
                }
                
                MachineDataSet machineDataSet = nDataSelectable
                                                    .where()
                                                        .eq(idFieldName, row_id)
                                                    .compile()
                                                    .executeSelect();

                
                while (machineDataSet.next()) {
                    
                    fields.forEach((field) -> {
                                                
                        if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_STRING) {
                            rowData.put(field.getFieldKey(),machineDataSet.getString(field.getFieldKey()));    
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_INT) {  
                            rowData.put(field.getFieldKey(),machineDataSet.getBigDecimal(field.getFieldKey()).toPlainString()); 
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_DOUBLE) {  
                            rowData.put(field.getFieldKey(),String.valueOf(machineDataSet.getDouble(field.getFieldKey()))); 
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_BOOLEAN) {
                            
                            if (machineDataSet.getBoolean(field.getFieldKey())) {
                                rowData.put(field.getFieldKey(),"on");
                            } else {
                                rowData.put(field.getFieldKey(),"off");
                            }
                            
                        }  else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_TIMESTAMP) {
                            
                            rowData.put(field.getFieldKey(),
                                    new SimpleDateFormat(field.getFormat())
                                            .format(new Date(machineDataSet
                                                    .getTimestamp(field.getFieldKey()).getTime())));
                            
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_JSON) {
                            
                            rowData.put(field.getFieldKey(),String.valueOf(machineDataSet.getBytes(field.getFieldKey())));
                            
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_DATE) {
                            
                            Date t_date =  machineDataSet.getDate(field.getFieldKey());
                            
                            if (t_date != null) {
                                rowData.put(field.getFieldKey(),new SimpleDateFormat(field.getFormat()).format(t_date));
                            } else {
                                rowData.put(field.getFieldKey(),null);
                            }
                            
                            
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_TIME) {
                            
                            rowData.put(field.getFieldKey(),machineDataSet.getTime(field.getFieldKey()).toString());
                            
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_DATETIME) {
                            
                            Timestamp t_timestamp = machineDataSet.getTimestamp(field.getFieldKey());
                            
                            if (t_timestamp != null) {
                                rowData.put(field.getFieldKey(),new SimpleDateFormat(field.getFormat()).format(new Date(t_timestamp.getTime())));
                            } else {
                                rowData.put(field.getFieldKey(),null);
                            }
                            
                        } else if (machineDataSet.getColumnType(field.getFieldKey())==DataTypes.TYPE_ENUM) {
                            
                            rowData.put(field.getFieldKey(),machineDataSet.getEnum(field.getFieldKey()));
                            
                        }
                    });
                    
                }
                
                

        return rowData;
        
    }


        public Map<Integer, Object> getFieldsForListUpdate(CrudTable t_crudtable, int row_id) throws ClassNotFoundException, SQLException, Exception {

                
                Map<String,Object> rowData = getRowDataFromDatabaseById(t_crudtable,t_crudtable.getListIdField(),row_id);
                

                final Map<Integer, Object> fieldsMap = new LinkedHashMap<>();    

                for (CrudField field : t_crudtable.getFields()) {                                  
                                    if (!field.isEditable()) continue;
                                    if (field.isListData()) continue;

                                    Map<String,Object> fieldOptions = new LinkedHashMap<>();

                                    fieldOptions.put("displayname", field.getDisplayName());

                                    if (field.isExternal() && field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values", field.getExternal());
                                            fieldOptions.put("selected", rowData.get(field.getFieldName()));

                                    } else if (field.isExternal() && !field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", field.getExternalFieldDataTypeAsString());

                                            MachineDataSet machineDataSetOfExternalField = null;
                                            try {
                                                
                                                 
                                                
                                                machineDataSetOfExternalField = new DataConnector(this.crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                                                                .getTable(field.getExternalTable())
                                                                                                .select()
                                                                                                    .getColumn(field.getExternalGetField())
                                                                                                .where()
                                                                                                    .eq(field.getExternalJoinField(), Integer.parseInt((String)rowData.get(field.getFieldKey())))
                                                                                                .compile()
                                                                                                .executeSelect();
                                            } catch (ClassNotFoundException | SQLException ex) {
                                                Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                            fieldOptions.put("values",machineDataSetOfExternalField.getFirstString(field.getExternalGetField()));

                                    } else {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", field.getDataTypeAsString());


                                            if (field.getDataType()==DataTypes.TYPE_ENUM) {
                                                fieldOptions.put("values",field.getEnumValues());
                                                fieldOptions.put("selected", rowData.get(field.getFieldName()));
                                            } else {
                                                fieldOptions.put("values", rowData.get(field.getFieldName()));
                                            }

                                    }                        
                                    fieldsMap.put(fieldsMap.size(), fieldOptions);


                 }
                
                                // add row_id field to field list
                                Map<String,Object> row_id_field = new LinkedHashMap<>();
                                                   row_id_field.put("name", "row_id");
                                                   row_id_field.put("datatype", "hidden");
                                                   row_id_field.put("values", row_id);
                                                   
                                fieldsMap.put(fieldsMap.size(), row_id_field);
                
                return fieldsMap;
        
    }  
    
    public Map<Integer, Object> getFieldForUpdateModel(CrudTable crudtable, int row_id) throws ClassNotFoundException, SQLException, Exception {
        
                
                Map<String,Object> rowData = getRowDataFromDatabaseById(crudtable,crudtable.getIdFieldName(),row_id);

                final Map<Integer, Object> fieldsMap = new LinkedHashMap<>();    

                for (CrudField field : crudtable.getFields()) {
                                  
                                    if (!field.isEditable()) continue;
                                    if (field.isListData()) continue;

                                    Map<String,Object> fieldOptions = new LinkedHashMap<>();

                                    fieldOptions.put("displayname", field.getDisplayName());

                                    if (field.isExternal() && field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values", field.getExternal());
                    
                                            
                                            fieldOptions.put("selected", rowData.get(field.getFieldKey()));

                                    } else if (field.isExternal() && !field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", field.getExternalFieldDataTypeAsString());

                                            try {
                                                
                                                MachineDataSet machineDataSetOfExternalField = new DataConnector(this.crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                                                                .getTable(field.getExternalTable())
                                                                                                .select()
                                                                                                    .setDebug(true)
                                                                                                    .getColumn(field.getExternalGetField())
                                                                                                .where()
                                                                                                    .eq(field.getExternalJoinField(), Integer.parseInt((String)rowData.get(field.getFieldKey())))
                                                                                                .compile()
                                                                                                .executeSelect();
                                                
                                                fieldOptions.put("values",machineDataSetOfExternalField.getFirst(field.getExternalGetField()));                                                
                                                
                                            } catch (ClassNotFoundException | SQLException ex) {
                                                Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                            

                                    } else {
                                        
                                        if (field.getStatementUpdate() == null) {
                                       

                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", field.getDataTypeAsString());


                                            if (field.getDataType()==DataTypes.TYPE_ENUM) {
                                                fieldOptions.put("values",field.getEnumValues());
                                                fieldOptions.put("selected", rowData.get(field.getFieldKey()));
                                            } else {
                                                if (field.isCrypted()) {
                                                    fieldOptions.put("values", "[ crypted ]");
                                                } else {
                                                    fieldOptions.put("values", rowData.get(field.getFieldKey()));
                                                }
                                            }
                                            
                                        } else {
                                            fieldOptions.put("name", field.getFieldKey());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values",getUpdateStatementValues(field));
                                            fieldOptions.put("selected", rowData.get(field.getFieldKey()));

                                        } 

                                    }                        
                                    fieldsMap.put(fieldsMap.size(), fieldOptions);


                                }
                
                                // add row_id field to field list
                                Map<String,Object> row_id_field = new LinkedHashMap<>();
                                                   row_id_field.put("name", "row_id");
                                                   row_id_field.put("datatype", "hidden");
                                                   row_id_field.put("values", row_id);
                                                   
                                fieldsMap.put(fieldsMap.size(), row_id_field);
                
                return fieldsMap;
        
    }    

    public Map<Integer, Object> getFieldsForListDataUpdate(CrudTable t_crudtable, int row_id) throws ClassNotFoundException, SQLException, Exception {
       

                Map<String,Object> rowData = getRowDataFromDatabaseById(t_crudtable,t_crudtable.getIdFieldName(),row_id);

                final Map<Integer, Object> fieldsMap = new LinkedHashMap<>();    

                for (CrudField field : t_crudtable.getFields()) {
                                  
                                    if (!field.isListData() || !field.isUseForAdd()) continue;

                                    Map<String,Object> fieldOptions = new LinkedHashMap<>();

                                    fieldOptions.put("displayname", field.getDisplayName());

                                    if (field.isExternal() && field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldName());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values", field.getExternal());
                                            fieldOptions.put("selected", rowData.get(field.getFieldName()));

                                    } else if (field.isExternal() && !field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldName());
                                            fieldOptions.put("datatype", field.getExternalFieldDataTypeAsString());

                                            MachineDataSet machineDataSetOfExternalField = null;
                                            try {
                                                machineDataSetOfExternalField = new DataConnector(this.crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                                                                .getTable(field.getExternalTable())
                                                                                                .select()
                                                                                                    .getColumn(field.getExternalGetField())
                                                                                                .where()
                                                                                                    .eq(field.getExternalJoinField(), Integer.parseInt((String)rowData.get(field.getFieldName())))
                                                                                                .compile()
                                                                                                .executeSelect();
                                            } catch (ClassNotFoundException | SQLException ex) {
                                                Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                            fieldOptions.put("values",machineDataSetOfExternalField.getFirstString(field.getExternalGetField()));

                                    } else {

                                            fieldOptions.put("name", field.getFieldName());
                                            fieldOptions.put("datatype", field.getDataTypeAsString());


                                            if (field.getDataType()==DataTypes.TYPE_ENUM) {
                                                fieldOptions.put("values",field.getEnumValues());
                                                fieldOptions.put("selected", rowData.get(field.getFieldName()));
                                            } else {
                                                fieldOptions.put("values", rowData.get(field.getFieldName()));
                                            }

                                    }                        
                                    fieldsMap.put(fieldsMap.size(), fieldOptions);


                 }
                
                                // add row_id field to field list
                                Map<String,Object> row_id_field = new LinkedHashMap<>();
                                                   row_id_field.put("name", t_crudtable.getListIdField());
                                                   row_id_field.put("datatype", "hidden");
                                                   row_id_field.put("values", row_id);
                                                   
                                fieldsMap.put(fieldsMap.size(), row_id_field);
                
                return fieldsMap;
        
    }    

    
    public Map<String,Map<String,Object>> getFilterFields(String t_crudobject,String t_crudtable) throws Exception {
                
                
                CrudTable crudTable = loader.get(t_crudobject).getCrudTable(t_crudtable);
                
                Collection<CrudFilterField> filters = crudTable.getReadFilterFields();
               
                Map<String,Map<String,Object>> filterFieldsMap = new LinkedHashMap<>();
                
                for (CrudFilterField crudFilterField : filters) {
                    
                      if (crudFilterField.getType().equals("fixed")) continue;
                      
                      Map<String,Object> fieldParams = new LinkedHashMap<>();

                      
                      CrudField field = crudTable.getField(crudFilterField.getFieldName());
                      
                            fieldParams.put("displayname",field.getDisplayName());

                            if (field.isExternal()) {

                                    fieldParams.put("name", field.getFieldName());
                                    fieldParams.put("datatype", "external");
                                    fieldParams.put("values", field.getExternal());
                                    fieldParams.put("selected", crudFilterField.getFieldValue());                                                

                            } else {

                                    fieldParams.put("name", field.getFieldName());
                                    fieldParams.put("datatype", field.getDataTypeAsString());


                                    if (field.getDataType()==DataTypes.TYPE_ENUM) {
                                        fieldParams.put("values",field.getEnumValues());
                                        fieldParams.put("selected",crudFilterField.getFieldValue() );
                                    }

                            }                        
                      
                      fieldParams.put("fieldDataType",field.getDataTypeAsString());

                      filterFieldsMap.put(crudFilterField.getFieldName(), fieldParams);
                }

                
                return filterFieldsMap;
        
    }    
    



    public Map<String,String> getUpdateStatementValues (CrudField field) throws Exception {
        

                                    MachineDataSet machineDataSet = new DataConnector(this.crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                                            .executeQuery(field.getStatementUpdate());
                                            
                                    
                                    Map<String,String> externalValues = new LinkedHashMap<>();


                                    
                                    
                                    if (field.getFieldName()==null) {
                                       throw new Exception("External Field \"" + field.getFieldName() + "\" is null");
                                    }


                                    int joinFieldDatatype = DataTypes.TYPE_INT;                                   
                                    
                                    while (machineDataSet.next()) {

                                            switch (joinFieldDatatype) {
                                                case DataTypes.TYPE_INT:
                                                    externalValues.put(String.valueOf(machineDataSet.getInteger(field.getExternalJoinField())),machineDataSet.getString(field.getExternalGetField()));
                                                    break;
                                                case DataTypes.TYPE_LONG:
                                                    externalValues.put(String.valueOf(machineDataSet.getLong(field.getExternalJoinField())),machineDataSet.getString(field.getExternalGetField()));
                                                    break;
                                                default:
                                                    externalValues.put(String.valueOf(machineDataSet.getInteger(field.getExternalJoinField())),machineDataSet.getString(field.getExternalGetField()));
                                                    break;
                                            }
                                        
                                        
                                    }
                                    
                                    return externalValues;
        
        
    }
    
    public Map<String,String> getCreateStatementValues (CrudField field) throws Exception {
        

                                    MachineDataSet machineDataSet = new DataConnector(this.crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                                            .executeQuery(field.getStatementCreate());
                                            
                                    
                                    Map<String,String> externalValues = new LinkedHashMap<>();


                                    
                                    
                                    if (field.getFieldName()==null) {
                                       throw new Exception("External Field \"" + field.getFieldName() + "\" is null");
                                    }


                                    int joinFieldDatatype = DataTypes.TYPE_INT;                                   
                                    
                                    while (machineDataSet.next()) {

                                            switch (joinFieldDatatype) {
                                                case DataTypes.TYPE_INT:
                                                    externalValues.put(String.valueOf(machineDataSet.getInteger(field.getExternalJoinField())),machineDataSet.getString(field.getExternalGetField()));
                                                    break;
                                                case DataTypes.TYPE_LONG:
                                                    externalValues.put(String.valueOf(machineDataSet.getLong(field.getExternalJoinField())),machineDataSet.getString(field.getExternalGetField()));
                                                    break;
                                                default:
                                                    externalValues.put(String.valueOf(machineDataSet.getInteger(field.getExternalJoinField())),machineDataSet.getString(field.getExternalGetField()));
                                                    break;
                                            }
                                        
                                        
                                    }
                                    
                                    return externalValues;
        
        
    }
    
    
    
}
