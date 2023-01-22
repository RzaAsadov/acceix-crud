/*
 * The MIT License
 *
 * Copyright 2022 Rza Asadov (rza at asadov dot me).
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
import org.acceix.frontend.helpers.ButtonsHelper;
import org.acceix.ndatabaseclient.mysql.DataTypes;
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
import org.acceix.ndatabaseclient.dataset.MachineDataSet;
import org.acceix.ndatabaseclient.mysql.DataComparable;
import org.acceix.ndatabaseclient.mysql.DataConnector;
import org.acceix.ndatabaseclient.mysql.DataDeletable;
import org.acceix.ndatabaseclient.mysql.DataInsertable;
import org.acceix.ndatabaseclient.mysql.DataSelectable;
import org.json.simple.parser.ParseException;


/**
 *
 * @author zrid
 */
public class ObjectReadOperations {
    
    ModuleHelper crudModule;
    
    ObjectLoader loader = new ObjectLoader();
    
    DataConnector dataConnector = null;

    public ObjectReadOperations(ModuleHelper crudModule) {
        this.crudModule = crudModule;
        dataConnector = new DataConnector(this.crudModule.getGlobalEnvs(),true,crudModule.getUsername());
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


                                        Map<String,Object> button_edit = new ButtonsHelper().createButton("Remove", 
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
                                                      
                            
                            
                            var button_view = new ButtonsHelper().createLink(
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
                            
                            var button_delete = new ButtonsHelper().createButton(
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
                            
                                    String cordinates_json_str = dataSet_files.getString((String)requestObject.getParams().get("locationfieldname"));
                                    
                                    if (cordinates_json_str.isEmpty()) {
                                        cordinates_json_str = "0";
                                    } else {
                                        Map cordinates = new DataUtils().readJsonObjectFromString(cordinates_json_str);

                                        fieldsMap.put("cordinates", cordinates);                                        
                                    }


                        } else {
                                    fieldsMap.put("cordinates", "0");
                        }

            
                    DataConnector dataConnector = new DataConnector(this.crudModule.getGlobalEnvs(),true,crudModule.getUsername());
                   


                    CrudField crudField = crudTable.getField((String)requestObject.getParams().get("locationfieldname"));
                        

                        
                    Map<String,Object> mapField = new LinkedHashMap<>();    
                        
                        mapField.put("apikey",crudField.getApiKey());
                        mapField.put("defaultlat",crudField.getDefaultLat());
                        mapField.put("defaultlong",crudField.getDefaultLong());
                        
                    
                    fieldsMap.put("mapfield", mapField);                       
                        
                    Map<String,Object> addressField = new LinkedHashMap<>();

                        addressField.put("displayname", "Address");
                        addressField.put("name", "address");
                        addressField.put("datatype", DataTypes.TYPE_STRING);
                        addressField.put("values", "");

                    fieldsMap.put("address", addressField);
                    
            

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
                            
                            var buttons = new ButtonsHelper();
                            
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
    




    
    
    
}
