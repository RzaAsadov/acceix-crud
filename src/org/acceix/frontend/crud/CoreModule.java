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

import org.acceix.frontend.crud.loaders.FunctionLoader;
import org.acceix.frontend.crud.models.CrudFunction;
import org.acceix.frontend.crud.loaders.ObjectLoader;
import org.acceix.frontend.crud.models.CrudInput;
import org.acceix.frontend.crud.models.CrudObject;
import org.acceix.frontend.crud.models.CrudTable;
import org.acceix.frontend.helpers.ActionSettings;
import org.acceix.frontend.helpers.ModuleHelper;
import org.acceix.ndatabaseclient.mysql.DataTypes;
import org.acceix.frontend.web.commons.DataUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.ndatabaseclient.exceptions.MachineDataException;
import org.acceix.ndatabaseclient.dataset.MachineDataSet;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;
import org.json.simple.parser.ParseException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zrid
 */
public class CoreModule extends org.acceix.frontend.helpers.ModuleHelper {
    
    private CrudObject crudObject;
    private CrudTable crudTable;   
    private ObjectCreateOperations crudCreateOperations;
    private ObjectReadOperations crudReadOperations;
    private ObjectUpdateOperations crudUpdateOperations;
    private ObjectDeleteOperations crudDeleteOperations;
    
    private ObjectCommonOperations crudCommonOperations;

    @Override
    public ModuleHelper getInstance() {
        return new CoreModule();
    }
    
    
    

    @Override
    public void construct() {
        
        setModuleName("crud");
        addAction(new ActionSettings("getcreatemodel", false, this::getcreatemodel));
        addAction(new ActionSettings("create", false, this::create));
        addAction(new ActionSettings("read", false, this::read));
        
        addAction(new ActionSettings("getupdatemodel", false, this::getupdatemodel));                
        addAction(new ActionSettings("update", false, this::update));
        addAction(new ActionSettings("delete", false, this::delete));
        addAction(new ActionSettings("getfiltermodel", true, this::getfiltermodel));
        
        
        addAction(new ActionSettings("function", false, this::function));
        addAction(new ActionSettings("getfunctionrunmodel", false, this::getfunctionrunmodel));
        addAction(new ActionSettings("apijs", false, this::apijs));  
        addAction(new ActionSettings("apijsasync", false, this::apijsasync));  
                        
        addAction(new ActionSettings("readlist", false, this::readlist));
        addAction(new ActionSettings("getlistupdatemodel", false, this::getlistupdatemodel));
        addAction(new ActionSettings("updatelistdata", false, this::updatelistdata));
        addAction(new ActionSettings("removeListElement", false, this::removeListElement));
        

    }
    
     
    public boolean setupAndChecks() {
        
                crudCreateOperations = new ObjectCreateOperations(this);
                crudReadOperations = new ObjectReadOperations(this);
                crudUpdateOperations = new ObjectUpdateOperations(this);
                crudDeleteOperations = new ObjectDeleteOperations(this);
                
                crudCommonOperations = new ObjectCommonOperations(this);
        
                String obj = (String) getParameter("obj");
                String table = (String) getParameter("table");
                
                
                if (!new ObjectLoader().isExist(obj)) {
                    addToDataModel("message", "Wrong object request !");
                    addToDataModel("result", "error");
                    renderData();
                    return false;
                }
                
                crudObject = new ObjectLoader().get(obj);
                
                if (crudObject != null) {
                
                        if ( crudObject.getCrudTables().keySet().size() >= 1 && table != null) {
                                crudTable = crudObject.getCrudTable(table);
                        } else if ( crudObject.getCrudTables().keySet().size() >= 1 && table == null ) {
                                crudTable = crudObject.getDefaultCrudTable();
                        } else if ( crudObject.getCrudTables().keySet().isEmpty() && table != null ) {
                                addToDataModel("message", "Unable find such table");
                                addToDataModel("result", "error");
                                renderData();
                                return false;
                        } else {
                                //addToDataModel("message", "Wrong system call !");
                                //addToDataModel("result", "error");
                                //renderData();
                                //return false;
                        }

                        if  (crudObject.isRequireAuth() && !isUserAuthenticatedBySession() && !isAuthenticatedByToken()) {
                            addToDataModel("message", "Something went wrong with request authentication!");
                            addToDataModel("authByToken",isAuthenticatedByToken());
                            addToDataModel("authBySession",isUserAuthenticatedBySession());
                            addToDataModel("objectRequireAuth",crudObject.isRequireAuth());
                            addToDataModel("result", "error");
                            renderData();
                            return false;
                        }

                        addToDataModel("cur_obj",crudObject.getName());
                        return true;
                        
                } else {
                        addToDataModel("message", "Wrong object name !");
                        addToDataModel("result", "error");
                        renderData();
                        return false;
                }
    }
    
    
    public void getcreatemodel() {
        
                if (setupAndChecks()==false) return;

                Map<Integer, Object> fieldsForCreate = null;
                try {
                    
                    fieldsForCreate = crudCreateOperations.getCreateModel(crudObject.getName(), crudTable.getName());
                    
                    if (fieldsForCreate==null) {
                        addToDataModel("message", "Unable to fetch create model !");
                        addToDataModel("result", "error");
                        renderData();
                        return;
                    }
                } catch (Exception ex) {
                    addToDataModel("message",ex.getMessage());
                    addToDataModel("result", "error"); 
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);     
                    renderData();
                    return;
                }
                
              
                
                
                //
                addToDataModel("doafter","loadContainerQuery('crud','read','#netondocontentbody','obj=" + crudObject.getName() + "');");                

                addToDataModel("submit_to_module", "crud");
                addToDataModel("submit_to_action", "create");                
                
                addToDataModel("fields", fieldsForCreate);
                
                crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                    addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                });
                
                try {
                    renderData(crudObject.getTemplateForCreate());
                } catch (IOException ex) {
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                }
        
    }
    
    public void getfunctionrunmodel() {
        
                String functionName = (String) getParameter("functionname");
                
                if (functionName == null) {
                    addToDataModel("message", "Wrong function request !");
                    addToDataModel("result", "error");
                    renderData();
                    return;                    
                }
                
                CrudFunction crudFunction = new FunctionLoader().get(functionName);
                
                Map<String,CrudInput> inputOfFunction =  crudFunction.getInputs();
                
                Map<Integer, Object> fieldsMap = new LinkedHashMap<>();
                
                DataTypes dataTypes = new DataTypes();
                
                inputOfFunction.entrySet().stream().map((inputField) -> {
                    Map<String,Object> fieldOptions = new LinkedHashMap<>();
                    fieldOptions.put("displayname", inputField.getValue().getInputName());
                    fieldOptions.put("name", inputField.getValue().getInputName());
                    fieldOptions.put("datatype", dataTypes.dataTypeToString(inputField.getValue().getDataType()));
                    fieldOptions.put("values", inputField.getValue().getTestValue());
                    return fieldOptions;
                }).forEachOrdered((fieldOptions) -> {
                    if (fieldOptions.get("datatype").equals("fixed")) { return; }
                    fieldsMap.put(fieldsMap.size(), fieldOptions);
                });


                addToDataModel("operationType",crudFunction.getOperationTypeAsString());     
                addToDataModel("submit_to_module", "crud");
                addToDataModel("submit_to_action", functionName);                
                

                addToDataModel("fields", fieldsMap);
                
        try {
            renderData(crudFunction.getTemplateForRun());
        } catch (IOException ex) {
            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void readlist() {
        

                if (setupAndChecks()==false) return;

                if (!isRoleAviableForUser(crudObject.getRoleRead())) {
                    
                        addToDataModel("result", "error");
                        addToDataModel("message", "Access denied !");
                        
                        try {
                            renderData(crudObject.getTemplateForListRead());
                        } catch (IOException ex) {
                            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        return;
                        
                }
                
                List<String> headers = new LinkedList<>();
                
                crudTable.getListFields().forEach( field-> { headers.add(field.getDisplayName()); });                
                if (crudObject.isEditable()) headers.add("Actions");
                addToDataModel("headers", headers);
                
                crudObject.setUser_id(getUserId());
                crudObject.setDomain(getDomain());
                
                    try {
                        addToDataModel("data", crudReadOperations.getListDataFromDatabase(crudObject,crudTable,crudTable.getListIdField(),
                                Integer.valueOf(getParameterOrDefault("row_id","-1")),                
                                getRequestObject()));
                    } catch (Exception ex) {
                        addToDataModel("message",ex.getMessage());
                        addToDataModel("result", "error"); 
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                    }
                

                addToDataModel("filterFields",true);
                addToDataModel("containstable", true);
                addToDataModel("creatable", crudObject.isCreatable());
                addToDataModel("editable", crudObject.isEditable());
                
                
                // Check role
                if (isRoleAviableForUser(crudObject.getRoleUpdate())) {

                        Map<Integer,Object> fieldsForUpdate=null;
                        try {
                            fieldsForUpdate = crudUpdateOperations.getFieldsForListDataUpdate(crudTable,
                                                                                        Integer.parseInt(getParameterOrDefault("row_id","-1")));
                        } catch (Exception ex) {
                            addToDataModel("message",ex.getMessage());
                            addToDataModel("result", "error"); 
                            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);                            
                            return;
                        } 

                        if (fieldsForUpdate==null) {
                            addToDataModel("result", "error");
                            addToDataModel("message", "unable_to_get_data_from_database");
                            return;
                        } else {
                            addToDataModel("fields", fieldsForUpdate);                    
                        }

                        addToDataModel("submit_to_module", "crud");
                        addToDataModel("submit_to_action", "updatelistdata");
                        
                }              

                crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                    addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                });


        try {
            //renderDataDebug();
            renderData(crudObject.getTemplateForListRead());
        } catch (IOException ex) {
            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
   
    public void removeListElement() {
        
                if (setupAndChecks()==false) {
                    return;
                }  
                
                // Check role
                if (!isRoleAviableForUser(crudObject.getRoleDelete())) {
                    return;
                } else {
                    
                }           
                
                int row_id = Integer.parseInt((String)getParameterOrDefault("row_id", "-1"));
                
                try {
                    crudDeleteOperations.deleteListElementFromDatabase(crudTable, row_id);
                    
                        List<String> headers = new LinkedList<>();

                        crudTable.getViewableFields().forEach( field-> { if (field.isListData()) return;headers.add(field.getDisplayName()); });                
                        if (crudObject.isEditable()) headers.add("Actions");
                        addToDataModel("headers", headers);

                        crudObject.setUser_id(getUserId());
                        crudObject.setDomain(getDomain());

                        addToDataModel("data", crudReadOperations.getListDataFromDatabase(crudObject,
                                                                                        crudTable,
                                                                                        crudTable.getListIdField(),
                                                                                        Integer.valueOf(getParameterOrDefault("list_id","-1")),
                                                                                        getRequestObject()));                


                        addToDataModel("filterFields",true);
                        addToDataModel("containstable", true);
                        addToDataModel("creatable", crudObject.isCreatable());
                        addToDataModel("editable", crudObject.isEditable());
                        
                            // Check role
                            if (isRoleAviableForUser(crudObject.getRoleUpdate())) {

                                    Map<Integer,Object> fieldsForUpdate=null;
                                    try {
                                        fieldsForUpdate = crudUpdateOperations.getFieldsForListDataUpdate(crudTable,
                                                                                                    Integer.parseInt(getParameterOrDefault("row_id","-1")));
                                    } catch (ClassNotFoundException | SQLException ex) {
                                        addToDataModel("message",ex.getMessage());
                                        addToDataModel("result", "error");                                         
                                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                    if (fieldsForUpdate==null) {
                                        addToDataModel("result", "error");
                                        addToDataModel("message", "unable_to_get_data_from_database");
                                        return;
                                    } else {
                                        addToDataModel("fields", fieldsForUpdate);                    
                                    }

                                    addToDataModel("submit_to_module", "crud");
                                    addToDataModel("submit_to_action", "updatelist");

                            }                          

                        crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                            addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                        });


                   
                } catch (ClassNotFoundException | SQLException ex) {
                    addToDataModel("message",ex.getMessage());
                    addToDataModel("result", "error");                    
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
        }
                
                try {
                    renderData(crudObject.getTemplateForListRead());
                } catch (IOException ex) {
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                }      
    }
        
    
    public void read() {

        
                if (setupAndChecks()==false) return;

                if (!isRoleAviableForUser(crudObject.getRoleRead())) {
                    
                        addToDataModel("result", "error");
                        addToDataModel("message", "Access denied, check roles !");
                        try {
                            renderData(crudObject.getTemplateForRead());
                        } catch (IOException ex) {
                            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return;
                        
                }
                
                List<String> headers = new LinkedList<>();
                
                crudTable.getViewableFields().forEach( field-> { if (field.isListData()) return; headers.add(field.getDisplayName()); });                
                if (crudObject.isEditable()) headers.add("Actions");
                
                addToDataModel("headers", headers);
                
                crudObject.setUser_id(getUserId());
                crudObject.setDomain(getDomain());
                
                    try {
                        addToDataModel("data", crudReadOperations.readDataFromDb(crudObject,
                                                                                    crudTable,
                                                                                    Integer.valueOf(getParameterOrDefault("row_id","-1")),                
                                                                                    getRequestObject()));
                        
                        addToDataModel("filterFields",true);
                        addToDataModel("containstable", true);
                        addToDataModel("creatable", crudObject.isCreatable());
                        addToDataModel("editable", crudObject.isEditable());  
                        addToDataModel("result", "success"); 
                        
                        
                    } catch (ClassNotFoundException | SQLException ex) {
                        
                        addToDataModel("message",ex.getMessage());
                        addToDataModel("result", "error"); 
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        
                    } catch (Exception ex) {
                        
                        addToDataModel("message",ex.getMessage());
                        addToDataModel("result", "error");
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        
                    }
                



                crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                    addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                });

                
        try {
            renderData(crudObject.getTemplateForRead());
        } catch (IOException ex) {
            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void getfiltermodel()  {

                if (setupAndChecks()==false) return;


                    try {
                        
                            var filterFieldsMap = crudReadOperations.getFilterFields(crudObject.getName(),crudTable.getName());

                            if (filterFieldsMap.size() > 0)  addToDataModel("fields", filterFieldsMap);
                        
                    } catch (Exception ex) {
                        addToDataModel("message",ex.getMessage());
                        addToDataModel("result", "error");                        
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                    }

 
                crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                    addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                });                
                
                try {
                    renderData(crudObject.getTemplateForFilters());
                } catch (IOException ex) {
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                }
        
    }    
    

    public void getlistupdatemodel() {

           
                if (setupAndChecks()==false) return;
                
                // Check role
                if (!isRoleAviableForUser(crudObject.getRoleUpdate())) {
                    return;
                }
                
                
                try {
                    
                        var fieldsForListUpdate = crudUpdateOperations.getFieldsForListUpdate(crudTable,Integer.parseInt(getParameterOrDefault("row_id","-1")));

                        if (fieldsForListUpdate==null) {
                            addToDataModel("result", "error");
                            addToDataModel("message", "unable_to_get_data_from_database");
                            return;
                        } else {
                            addToDataModel("fields", fieldsForListUpdate);                    
                        } 
                    
                } catch (Exception ex) {
                    addToDataModel("result", "error");
                    addToDataModel("message", "unable_to_get_data_from_database");                    
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                } 

                
                addToDataModel("doafter","loadContainerQuery('crud','read','#netondocontentbody','obj=" + crudObject.getName() + "');");                

                addToDataModel("submit_to_module", "crud");
                addToDataModel("submit_to_action", "update");
                
                
                
                crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                    addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                });                
                
        try {
            renderData(crudObject.getTemplateForUpdate());
        } catch (IOException ex) {
            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } 
    
    
    public void getupdatemodel() {

           
                if (setupAndChecks()==false) return;
                
                // Check role
                if (!isRoleAviableForUser(crudObject.getRoleUpdate())) {
                    return;
                }
                
                
                try {
                    
                        var fieldsForUpdate = crudUpdateOperations.getFieldsForUpdateModel(crudTable, Integer.parseInt(getParameterOrDefault("row_id","-1")));

                        if (fieldsForUpdate==null) {
                            addToDataModel("result", "error");
                            addToDataModel("message", "unable_to_get_data_from_database");
                        } else {
                            addToDataModel("fields", fieldsForUpdate);  
                            addToDataModel("isdeletable",crudObject.isDeletable());
                        }    
                    
                } catch (Exception ex) {
                    addToDataModel("result", "error");
                    addToDataModel("message", ex.getMessage());                    
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                }
                

                
                addToDataModel("doafter","loadContainerQuery('crud','read','#netondocontentbody','obj=" + crudObject.getName() + "');");                

                addToDataModel("submit_to_module", "crud");
                addToDataModel("submit_to_action", "update");
                
                
                
                crudObject.getMetaDataKeys().forEach((metadata_key) -> {
                    addToDataModel(metadata_key, crudObject.getMetaData(metadata_key));
                });                
                
        try {
            renderData(crudObject.getTemplateForUpdate());
        } catch (IOException ex) {
            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } 
    
    public void updatelistdata() {
                if (setupAndChecks()==false) {
                    return;
                }  
                var inputParamsFromClient = getRequestObject().getParams();
                
                
                int result;
                try {
                    result = crudCreateOperations.createInList(crudObject, crudTable, inputParamsFromClient);
                } catch (SQLException | ClassNotFoundException ex) {
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                    addToDataModel("message", ex.getMessage()); 
                    addToDataModel("result", "error");  
                    renderData();
                    return;                      
                }

                if (result < -1) {
                        addToDataModel("message", "Unable to create !"); 
                        addToDataModel("result", "error");  
                        renderData();
                        return;                     
                } else if (result == -1) {
                       addToDataModel("message", "Unable to autogenerate id from database"); 
                       addToDataModel("result", "error");  
                       renderData();
                       return;
                }
                
                addToDataModel("message","Data created !");
                addToDataModel("result", "success");
                renderData();

    }    
    
    public void update() {

           
                if (setupAndChecks()==false) {
                    return;
                }  
                
                // Check role
                if (!isRoleAviableForUser(crudObject.getRoleUpdate())) {
                    return;
                } else {
                    
                }                

                
                
                var inputParams = getRequestObject().getParams();
                
                
                int row_id = Integer.parseInt((String)getParameterOrDefault("row_id", "-1"));
                
                var t_inputParams = new LinkedHashMap<String,Object>();

                var iterator = inputParams.keySet().iterator();
                
                while (iterator.hasNext()) {
                    
                    
                    String key = iterator.next();
                    if (key.equals("obj")) continue; // bypass object key
                    if (key.equals("row_id")) continue; // bypass row_id key

                    var a_crudField = crudTable.getField(key);
                    
                    if (a_crudField.isExternal()) {
                            if (a_crudField.isExternalForCreate()) {
                                t_inputParams.put(key, inputParams.get(key));
                            } else { // If we need update in external table
                                
                                String objectToInsert = a_crudField.getExternalObject();
                                String tableToInsert = a_crudField.getExternalTable();
                                String fieldToInsert = a_crudField.getExternalGetField();
                                String valueToInsert = (String)inputParams.get(key);
                                
                                Map<String,Object> external_inputParams = new LinkedHashMap<>();
                                
                                external_inputParams.put(fieldToInsert, valueToInsert);

                                
                                int rowEffected = crudCommonOperations.updateInDatabaseAndGetId(objectToInsert,
                                                                                                tableToInsert,
                                                                                                external_inputParams,
                                                                                                row_id );
                                
                                if (rowEffected <= 0) { 
                                    addToDataModel("error", "can_not_update_on_database_level"); 
                                    renderData(); 
                                    return; 
                                }
                            }
                    } else {

                        t_inputParams.put(key, inputParams.get(key));
                    }
                    
                }
                
                if (t_inputParams.size() > 0)
                    crudCommonOperations.updateInDatabaseAndGetId(crudObject.getName(),crudTable.getName(), t_inputParams,row_id);       
                
                addToDataModel("message","Data in object \"" + crudObject.getTitle()+ "\" updated !");
                addToDataModel("result", "success");
                renderData();                
                
        
    }
    
    public void delete() {
           
        
                if (setupAndChecks()==false) {
                    return;
                }  
                
                // Check role
                if (!isRoleAviableForUser(crudObject.getRoleDelete())) {
                    return;
                } else {
                    
                }           
                
                int row_id = Integer.parseInt((String)getParameterOrDefault("row_id", "-1"));
                
                crudDeleteOperations.deleteElementFromDatabase(crudTable, row_id);
                

                addToDataModel("message","Data in object \"" + crudObject.getTitle()+ "\" Deleted !");
                addToDataModel("result", "success");
                
                renderData();
        
    }
    
    public void create() {
        
                if (setupAndChecks()==false) {
                    return;
                }  
                
                var inputParamsFromClient = getRequestObject().getParams();

                
                int result;
                try {
                    result = crudCreateOperations.prepareForCreate(crudObject, crudTable, inputParamsFromClient);
                } catch (Exception ex) {
                    Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                    addToDataModel("message", ex.getMessage()); 
                    addToDataModel("result", "error");  
                    renderData();
                    return;                      
                }

                
                if (result < -1) {
                        addToDataModel("message", "Unable to create !"); 
                        addToDataModel("result", "error");  
                        renderData();
                        return;                     
                } else if (result == -1) {
                       addToDataModel("message", "Unable to autogenerate id from database"); 
                       addToDataModel("result", "error");  
                       renderData();
                       return;
                }
                
                
                addToDataModel("message","Data created !");
                addToDataModel("result", "success");
                renderData();

    }
    
    
    
    
    public void function() {
        
            
        
            initNewDataModel();        
        
            var params = getRequestObject().getParams();
            
            var functionAction = getRequestObject().getAction();
            
            var crudFunction = new FunctionLoader().get(functionAction);
            
            if (crudFunction == null) {

                NLogger.logger(NLogBlock.FUNCTIONS,NLog.ERROR,getModuleName(),"crud",getUsername(), "Function \"" + functionAction + "\" not found !");
                return;
            }
            
            // Set environment variable for function
            crudFunction.setDomain(getDomain());
            crudFunction.setUser_id(getUserId());            

            // Check role
            if (crudFunction.isRequireAuth() && !isUserAuthenticatedBySession() && !isAuthenticatedByToken()) {


                            addToDataModel("message", "Something went wrong with request authentication!");
                            addToDataModel("authByToken",isAuthenticatedByToken());
                            addToDataModel("authBySession",isUserAuthenticatedBySession());
                            addToDataModel("objectRequireAuth",crudFunction.isRequireAuth());
                            addToDataModel("result", "error");
                            renderData();
                            
                            return;                

            } 
            

                if (crudFunction.isRequireAuth() && !isRoleAviableForUser(crudFunction.getRoleRun())) {
   
                    NLogger.logger(NLogBlock.AUTH,NLog.ERROR,getModuleName(),"crud",getUsername(), "No access for user \"" + getUsername() + "\" to Function \"" + functionAction + "\"");

                    addToDataModel("message","your access to function denied !");
                    addToDataModel("result", "error");
                    renderData();                      
                    return;
                    
                } 
          
            
            var inputValues = new LinkedHashMap<String,Object>();
            
            crudFunction.getInputs().forEach( (k,v) -> {
                
                if (params.get(v.getInputName())!= null) {
                    if (v.getDataType()!=CrudInput.FIXED_TYPE) {
                        inputValues.put(v.getInputName(), (String)params.get(v.getInputName()));
                    }
                }
                
            });
            

            int inputSize = crudFunction.getInputs().size();
            

            
            for (Map.Entry<String,CrudInput> input : crudFunction.getInputs().entrySet()) {
                if (input.getValue().getDataType()==CrudInput.PAYLOAD_TYPE) {
                    inputSize--;
                } else if (input.getValue().getDataType()==CrudInput.FIXED_TYPE) {
                    inputSize--;
                }
            }

            //System.out.println("we have: " + inputValues.size() + " real is:" + inputSize);
            
            if (inputValues.size() < inputSize) {
                
                    NLogger.logger(NLogBlock.WEB_CRUD,NLog.ERROR,getModuleName(),"crud",getUsername(), "Missing input params for Function \"" + functionAction + "\"");
                    addToDataModel("message","missing input values!");
                    addToDataModel("result", "error");
                    renderData();  
                    return;
                    
            }
            

            
            var functionOperations = new FunctionOperations(this);
                        

            if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_CREATE) {
                
                int id = functionOperations.runCreate(crudFunction, inputValues);
                if (id > -1) {
                    addToDataModel("message","function executed!");
                    addToDataModel("id", id);
                    addToDataModel("result", "success");
                    renderData();  
                }
                
            } else if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_READ) {
                
                MachineDataSet machineDataSet = functionOperations.runRead(crudFunction, inputValues);
                if (machineDataSet != null) {
                    
                        addToDataModel("message","function executed!");
                        addToDataModel("result", "success");
                    
                    try {  
                        
                            if (getParameter("mode") != null && getParameter("mode").equals("test")) {

                                    DataUtils dataUtils = new DataUtils();    
                                    addToDataModel("data", machineDataSet.getResultAsMap());
                                    addToDataModel("coreresponsejson", dataUtils.beautyfyJson(dataUtils.mapToJsonString(getDataModel())));


                                    addToDataModel("corerequestjson", dataUtils.beautyfyJson(dataUtils.listToJsonString(getRequestObject().getRawInput())));

                                    renderData("/defaultTemplates/showFunctionData");

                            } else {
                                
                                addToDataModel("data", machineDataSet.getResultAsMap());
                                renderData(crudFunction.getTemplateForResult());
                                
                            }
                        
                    } catch (IOException ex) {
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }                
                
            } else if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_UPDATE) {
                if (functionOperations.runUpdate(crudFunction, inputValues)) {
                    addToDataModel("message","function executed!");
                    addToDataModel("result", "success");
                    renderData();  
                    
                }
            } else if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_DELETE) {
                if (functionOperations.runDelete(crudFunction, inputValues)) {
                    addToDataModel("message","function executed!");
                    addToDataModel("result", "success");
                    renderData();  
                }                
                
            }  else if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_FUNCTION) {
                
                    Map result = null;
                    SQLException sqlError = null;
                    try {
                        result = functionOperations.runFunction(crudFunction, inputValues,getRequestObject());
                    } catch (ParseException | IOException | MachineDataException ex) {
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        if (getParameter("mode") != null && getParameter("mode").equals("test")) {
                            
                        } else {
                            addToDataModel("result","error");
                            addToDataModel("message","Function not executed properly");
                            renderData();   
                            return;
                        }
                    } catch (SQLException ex) {
                        sqlError = ex;
                    }
                    
                    //if (result != null) {
                        
                            addToDataModel("message","function executed!");
                            addToDataModel("result", "success");


                        try {

                                if (getParameter("mode") != null && getParameter("mode").equals("test")) {
                                    DataUtils dataUtils = new DataUtils();  
                                    if (sqlError!=null) {
                                        addToDataModel("data", sqlError.getMessage());
                                    } else {
                                        addToDataModel("data", result);
                                    }
                                    addToDataModel("coreresponsejson", dataUtils.beautyfyJson(dataUtils.mapToJsonString(getDataModel())));
                                        // FOR TEST MODE
                                        Map ofJsonAddtionalKey = new LinkedHashMap();
                                        ofJsonAddtionalKey.put("name", "of");
                                        ofJsonAddtionalKey.put("value", "json");

                                        getRequestObject().getRawInput().add(ofJsonAddtionalKey);


                                        Map tokenAddtionalKey = new LinkedHashMap();
                                        tokenAddtionalKey.put("name", "token");
                                        tokenAddtionalKey.put("value", getToken()); 
                                        getRequestObject().getRawInput().add(tokenAddtionalKey);
                                    
                                    addToDataModel("corerequestjson", dataUtils.beautyfyJson(dataUtils.listToJsonString(getRequestObject().getRawInput())));
                                    renderData("/defaultTemplates/showFunctionData");
                                } else {     
                                    if (sqlError!=null) {
                                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, sqlError);
                                    }
                                    addToDataModel("data",result);
                                    renderData(crudFunction.getTemplateForResult());
                                }


                                
                        } catch (IOException ex) {
                            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        }                    

                //}                
                
            }  else if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_EXECUTABLE) {
                
                    Map result;
                    try {
                        result = functionOperations.runExecutable(crudFunction, inputValues,getRequestObject());
                    } catch (ParseException | IOException | MachineDataException ex) {
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        addToDataModel("result","error");
                        addToDataModel("message","Function not executed properly");
                        renderData();   
                        return;
                    }
                    
                    if (result != null) {
                        
                            addToDataModel("message","function executed!");
                            addToDataModel("result", "success");


                        try {

                                if (getParameter("mode") != null && getParameter("mode").equals("test")) {
                                    DataUtils dataUtils = new DataUtils();    
                                    addToDataModel("data", result);
                                    addToDataModel("coreresponsejson", dataUtils.beautyfyJson(dataUtils.mapToJsonString(getDataModel())));
                                        // FOR TEST MODE
                                        Map ofJsonAddtionalKey = new LinkedHashMap();
                                        ofJsonAddtionalKey.put("name", "of");
                                        ofJsonAddtionalKey.put("value", "json");

                                        getRequestObject().getRawInput().add(ofJsonAddtionalKey);


                                        Map tokenAddtionalKey = new LinkedHashMap();
                                        tokenAddtionalKey.put("name", "token");
                                        tokenAddtionalKey.put("value", getToken()); 
                                        getRequestObject().getRawInput().add(tokenAddtionalKey);
                                        
                                    addToDataModel("corerequestjson", dataUtils.beautyfyJson(dataUtils.listToJsonString(getRequestObject().getRawInput())));
                                    renderData("/defaultTemplates/showFunctionData");
                                } else {                        
                                    addToDataModel("data",result);
                                    renderData(crudFunction.getTemplateForResult());
                                }


                        } catch (IOException ex) {
                            Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                        }                    

                }                
                
            } else if (crudFunction.getOperationType()==CrudFunction.OPERATION_TYPE_PROCEDURE) {
                
                MachineDataSet machineDataSet = functionOperations.runProcedure(crudFunction, inputValues);
                if (machineDataSet != null) {
                    addToDataModel("message","function executed!");
                    addToDataModel("result", "success");
                    DataUtils dataUtils = new DataUtils();
                    

                    try {  
                        addToDataModel("data", dataUtils.beautyfyJson(dataUtils.mapToJsonString(machineDataSet.getResultAsMap())));
                        renderData(crudFunction.getTemplateForResult());
                    } catch (IOException ex) {
                        Logger.getLogger(CoreModule.class.getName()).log(Level.SEVERE, null, ex);
                    }                     

                }                
                
            } else {

            
                addToDataModel("message","Something gone wrong!");
                addToDataModel("result", "error");
                renderData();      
            
            }
        
        
    }
    
    public void apijsasync() {
        apijscore(true);
    }
    
    public void apijs() {
        apijscore(false);
    }
    
    
    public void apijscore(boolean is_async) {
        
       List<CrudFunction> funcList = new FunctionLoader().getList();
       
       String constructor = "";
       String func_to_call_if_async = "";
       String call_back_code = "";
       String return_data = "";
       
       if (is_async) {
           constructor = "constructor (funcname,func_to_call_ref)";
           func_to_call_if_async = "this.func_to_call = func_to_call_ref";
           call_back_code = "this.func_to_call( JSON.parse(data).data)";
       } else {
           constructor = "constructor (funcname)";
           return_data = "return data_from_srv";
           call_back_code = "data_from_srv = JSON.parse(data).data";
       }
       
           String callApiFunction = "class callApi {\n" +
                                    "    \n" +
                                    "        " +  constructor + " {\n" +
                                    "            this.data_to_srv = new Array();\n" +
                                    "            this.funcname = funcname;\n" +
                                    "            this.data_to_srv.push({\"name\":\"module\",\"value\": \"crud\"});\n" +
                                    "            this.data_to_srv.push({\"name\":\"action\",\"value\": funcname});\n" +
                                    "            this.data_to_srv.push({\"name\":\"of\",\"value\": \"json\"});            \n" +
                                    "            " + func_to_call_if_async  + ";\n" +
                                    "        }\n" +
                                    "        \n" +
                                    "    \n" +
                                    "        add (m_key,m_value) {\n" +
                                    "            this.data_to_srv.push({ \"name\":m_key, \"value\": m_value});\n" +
                                    "        }\n" +
                                    "        \n" +
                                    "\n" +
                                    "        getData() {\n" +
                                    "                        var data_from_srv;\n" +
                                    "                        $.ajax({\n" +
                                    "                                type: 'POST',\n" +
                                    "                                url: '"+ getGlobalEnvs().get("api_url") +"',\n" +
                                    "                                data: JSON.stringify(this.data_to_srv),\n" +
                                    "                                contentType: \"application/json\",\n" +
                                    "                                dataType: 'text',\n" +
                                    "                                async: " + String.valueOf(is_async) + ",\n" +
                                    "                                cache: false,\n" +
                                    "                                func_to_call: this.func_to_call,\n" +
                                    "                                success: function(data)  {\n" +
                                    "                                   " + call_back_code + ";\n" +
                                    "                                }\n" +
                                    //"                                success: " + call_back_code + "\n" +                  
                                    "                        }); \n" +
                                    "                        " + return_data + ";\n" +
                                    "\n" +
                                    "        };\n" +

                                    "}\n\n";
           
           sendToClient(callApiFunction);

            funcList.forEach(crudFunction -> {


                sendToClient("function " + crudFunction.getName() + " ");

                StringJoiner argumentList = new StringJoiner(",", "(", ")");
                
                argumentList.add("callback_func");

                crudFunction.getInputs().entrySet().forEach(entry -> {
                    argumentList.add(entry.getKey());
                });

                sendToClient(argumentList.toString() + " ");
                sendToClient(" {\n");


                     sendToClient("\tmyApiCall = new callApi(\""+ crudFunction.getName() + "\",callback_func);\n");
                     crudFunction.getInputs().entrySet().forEach(entry -> {
                         sendToClient("\tmyApiCall.add(\""+ entry.getKey() +"\"," + entry.getKey() + ");\n");
                     });

                     sendToClient("\t return myApiCall.getData();\n");


                sendToClient("}\n\n");

             });
            
       String messageLongPool = "async function subscribeToMessages(function_to_call,interval_seconds) {\n" +
                    "            \n" +
                    "                  let response = await fetch(\"" + getGlobalEnvs().get("api_url") + "?module=crud&action=getMessages&of=json\");\n" +
                    "                \n" +
                    "                  if (response.status == 502) {\n" +
                    "                    await subscribeToMessages(function_to_call,interval_seconds);\n" +
                    "                  } else if (response.status != 200) {\n" +
                    "                    console.log(response.statusText);\n" +
                    "                    await new Promise(resolve => setTimeout(resolve, interval_seconds));\n" +
                    "                    await subscribeToMessages(function_to_call,interval_seconds);\n" +
                    "                  } else {\n" +
                    "                    let message = await response.text();\n" +               
                    "                    parsed_data = JSON.parse(message);\n" +
                    "                    if (parsed_data.hasOwnProperty('data')) function_to_call(parsed_data.data);\n" +
                    "                    await new Promise(resolve => setTimeout(resolve, interval_seconds));\n" +
                    "                    await subscribeToMessages(function_to_call,interval_seconds);\n" +
                    "                  }\n" +
                    "}";
       
        sendToClient(messageLongPool);
       
       getOutputWriter().flush();
       getOutputWriter().close();
       
    }    
    
    
    

  
    
    
}
