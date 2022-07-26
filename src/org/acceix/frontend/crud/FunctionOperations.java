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
import org.acceix.frontend.crud.models.CrudFunction;
import org.acceix.frontend.crud.models.CrudInput;
import org.acceix.frontend.helpers.RequestObject;
import org.acceix.ndatabaseclient.DataTypes;
import org.acceix.frontend.web.commons.DataUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.ndatabaseclient.MachineDataSet;
import org.acceix.ndatabaseclient.DataConnector;
import org.acceix.ndatabaseclient.MachineDataException;
import org.acceix.ndatabaseclient.ResultSetConverter;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author zrid
 */
public class FunctionOperations {
    
    CoreModule crudModule;
    
    private static final int COMMAND_MODE_WAIT=0;
    private static final int COMMAND_MODE_NOWAIT=1;
    
    private int command_mode = COMMAND_MODE_WAIT;
    

    public FunctionOperations(CoreModule crudModule) {
        this.crudModule = crudModule;
    }

    public int runCreate(CrudFunction crudFunction,Map<String,Object> inputValues) {
        
        DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
        
        String content = crudFunction.getContent();
        
        try {
            PreparedStatement preparedStatement = dataConnector.getConnection().prepareStatement(content,Statement.RETURN_GENERATED_KEYS);
            
            Map<String,CrudInput> inputs = crudFunction.getInputs();
            int index = 1;

            for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                
                    CrudInput inputObj = entry.getValue();

                    if (inputObj.getDataType()==CrudInput.INTEGER_FIELD_TYPE) {
                            preparedStatement.setInt(index, Integer.parseInt((String)inputValues.get(inputObj.getInputName())));
                    } else if (inputObj.getDataType()==CrudInput.STRING_FIELD_TYPE) {
                            preparedStatement.setString(index, (String)inputValues.get(inputObj.getInputName()));
                    } else if (inputObj.getDataType()==CrudInput.TIMESTAMP_FIELD_TYPE) {
                            preparedStatement.setTimestamp(index, new Timestamp(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    } else if (inputObj.getDataType()==CrudInput.DATE_FIELD_TYPE) {
                            preparedStatement.setDate(index, new Date(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    }
                    index++;
                
            }
            
            
            preparedStatement.executeUpdate();
            
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                int myId = rs.getInt(1);
                preparedStatement.close();
                dataConnector.closeConnection();
                return myId;
            } else {
                return -1;
            }               
 
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }


    }

    public MachineDataSet runRead(CrudFunction crudFunction,Map<String,Object> inputValues) {
        
        DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
        
        String content = crudFunction.getContent();
        
        if (content.isEmpty()) {
            content = "select 1 as emptyResponse";
        }
        
        try {
            
            if (content.contains("$user_id$")) {
                content = content.replace("$user_id$", String.valueOf(crudFunction.getUser_id()));
            }
            
            if (content.contains("$domain$")) {
                content = content.replace("$domain$", crudFunction.getDomain());
            }
            
            MachineDataSet result;
            try (PreparedStatement preparedStatement = dataConnector.getConnection().prepareStatement(content)) {
                
                Map<String,CrudInput> inputs = crudFunction.getInputs();
                
                if (inputs.size() > 0) {
                    
                        int index = 1;

                        for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                            
                            CrudInput inputObj = entry.getValue();

                            if (inputObj.getDataType()==CrudInput.INTEGER_FIELD_TYPE) {
                                preparedStatement.setInt(index, Integer.parseInt((String)inputValues.get(inputObj.getInputName())));
                            } else if (inputObj.getDataType()==CrudInput.STRING_FIELD_TYPE) {
                                preparedStatement.setString(index, (String)inputValues.get(inputObj.getInputName()));
                            } else if (inputObj.getDataType()==CrudInput.TIMESTAMP_FIELD_TYPE) {
                                preparedStatement.setTimestamp(index, new Timestamp(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                            } else if (inputObj.getDataType()==CrudInput.DATE_FIELD_TYPE) {
                                preparedStatement.setDate(index, new Date(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                            }

                            index++;
                        }
                    
                }   result = new ResultSetConverter().resultSetToMachineDataSet(preparedStatement.executeQuery());
            }
            
            dataConnector.closeConnection();
            
            return result;
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    
    public Map runExecutable (CrudFunction crudFunction,Map<String,Object> inputValues,RequestObject requestObject) throws ParseException, IOException, MachineDataException {
        

        
        try {

                Map<String,CrudInput> inputs = crudFunction.getInputs();

                
                List arguments = new LinkedList();
                
                for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                    
                        CrudInput inputObj = entry.getValue();

                        String arg;

                        if (inputObj.getDataType()==DataTypes.TYPE_FIXED) {
                            
                            arg = (String)inputObj.getFixedValue();


                            if (arg.contains("$user_id$")) {
                                arg = arg.replace("$user_id$", String.valueOf(crudFunction.getUser_id()));
                            } else if (arg.contains("$domain$")) {
                                arg = arg.replace("$domain$", crudFunction.getDomain());
                            } else if (arg.contains("$env_mode$")) {
                                arg = arg.replace("$env_mode$", crudModule.getGlobalEnvs().get("env_mode").toString());
                            } else {
                                arg = (String)inputObj.getFixedValue();
                            }
                            

                        } else {
                            arg = (String)inputValues.get(inputObj.getInputName());
                        }

                        arguments.add(arg);

                }
                

                
                return handleNetondoBlock(runExternalScript(crudFunction.getContent(), arguments));
                    
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }       
    
    public Map runFunction (CrudFunction crudFunction,Map<String,Object> inputValues,RequestObject requestObject) throws ParseException, IOException, MachineDataException, SQLException {
        
        DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
        
        String content = crudFunction.getContent();
        
        try {
            
            if (content.contains("$user_id$")) {
                content = content.replace("$user_id$", String.valueOf(crudFunction.getUser_id()));
            }
            
            if (content.contains("$domain$")) {
                content = content.replace("$domain$", crudFunction.getDomain());
            }
            
            
            if (content.contains("$env_mode$")) {
                content = content.replace("$env_mode$", crudModule.getGlobalEnvs().get("env_mode").toString());
            }            
         
            
            String returnValue;
            Connection conn = dataConnector.getConnection();
            
            if (conn==null) {
                return  null;
            }
            
            try (PreparedStatement preparedStatement = conn.prepareStatement(content)) {
                
                    Map<String,CrudInput> inputs = crudFunction.getInputs();
                    
                    int index = 1;
                    for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                        CrudInput inputObj = entry.getValue();

                        if (inputObj.getDataType()==CrudInput.INTEGER_FIELD_TYPE) {
                            preparedStatement.setInt(index, Integer.parseInt((String)inputValues.get(inputObj.getInputName())));
                        } else if (inputObj.getDataType()==CrudInput.STRING_FIELD_TYPE) {
                            preparedStatement.setString(index, (String)inputValues.get(inputObj.getInputName()));
                        } else if (inputObj.getDataType()==CrudInput.TIMESTAMP_FIELD_TYPE) {
                            preparedStatement.setTimestamp(index, new Timestamp(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                        } else if (inputObj.getDataType()==CrudInput.DATE_FIELD_TYPE) {
                            preparedStatement.setDate(index, new Date(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                        } else if (inputObj.getDataType()==CrudInput.PAYLOAD_TYPE) {
                            
                            //NLogger.logger(NLogBlock.FUNCTIONS, NLog.MESSAGE,"FunctionOperations","runFunction",crudModule.getUsername(), "WEBHOOK RECEIVED (" + crudFunction.getName() +  ")-> " + requestObject.getRequestBody());

                            preparedStatement.setString(index, requestObject.getRequestBody());
                        }


                        index++;
                    }
                    
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                
                        returnValue = (String) new ResultSetConverter().resultSetToMachineDataSet(resultSet)
                                                                        .getResultAsMap()
                                                                        .get(0)
                                                                        .entrySet()
                                                                        .iterator()
                                                                        .next()
                                                                        .getValue();
                        
                    }
                
                
                
            } 
            dataConnector.closeConnection();
            return handleNetondoBlock(returnValue);
                    

            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }   

    public MachineDataSet runProcedure(CrudFunction crudFunction,Map<String,Object> inputValues) {
        
        DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
        
        String content = crudFunction.getContent();
        
        try {
            
            if (content.contains("$user_id$")) {
                content = content.replace("$user_id$", String.valueOf(crudFunction.getUser_id()));
            }
            
            if (content.contains("$domain$")) {
                content = content.replace("$domain$", crudFunction.getDomain());
            }
            
            MachineDataSet resultDataSet;
            
            try (PreparedStatement preparedStatement = dataConnector.getConnection().prepareStatement(content)) {
                Map<String,CrudInput> inputs = crudFunction.getInputs();
                int index = 1;
                
                for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                    CrudInput inputObj = entry.getValue();
                    
                    if (inputObj.getDataType()==CrudInput.INTEGER_FIELD_TYPE) {
                        preparedStatement.setInt(index, Integer.parseInt((String)inputValues.get(inputObj.getInputName())));
                    } else if (inputObj.getDataType()==CrudInput.STRING_FIELD_TYPE) {
                        preparedStatement.setString(index, (String)inputValues.get(inputObj.getInputName()));
                    } else if (inputObj.getDataType()==CrudInput.TIMESTAMP_FIELD_TYPE) {
                        preparedStatement.setTimestamp(index, new Timestamp(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    } else if (inputObj.getDataType()==CrudInput.DATE_FIELD_TYPE) {
                        preparedStatement.setDate(index, new Date(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    }
                    
                    index++;
                }
                
                
                resultDataSet = new ResultSetConverter().resultSetToMachineDataSet(preparedStatement.executeQuery());

            }

            return resultDataSet;
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }       
    
    
    public boolean runUpdate(CrudFunction crudFunction,Map<String,Object> inputValues) {
        
        DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
        
        String content = crudFunction.getContent();
        
            
            if (content.contains("$user_id$")) {
                content = content.replace("$user_id$", String.valueOf(crudFunction.getUser_id()));
            }
            
            if (content.contains("$domain$")) {
                content = content.replace("$domain$", crudFunction.getDomain());
            }
            
            
            if (content.contains("$env_mode$")) {
                content = content.replace("$env_mode$", crudModule.getGlobalEnvs().get("env_mode").toString());
            }           
        
        try {
            try (PreparedStatement preparedStatement = dataConnector.getConnection().prepareStatement(content)) {
                Map<String,CrudInput> inputs = crudFunction.getInputs();
                int index = 1;
                
                for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                    CrudInput inputObj = entry.getValue();
                    
                    if (inputObj.getDataType()==CrudInput.INTEGER_FIELD_TYPE) {
                        preparedStatement.setInt(index, Integer.parseInt((String)inputValues.get(inputObj.getInputName())));
                    } else if (inputObj.getDataType()==CrudInput.STRING_FIELD_TYPE) {
                        preparedStatement.setString(index, (String)inputValues.get(inputObj.getInputName()));
                    } else if (inputObj.getDataType()==CrudInput.TIMESTAMP_FIELD_TYPE) {
                        preparedStatement.setTimestamp(index, new Timestamp(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    } else if (inputObj.getDataType()==CrudInput.DATE_FIELD_TYPE) {
                        preparedStatement.setDate(index, new Date(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    }
                    index++;
                }
                
                
                preparedStatement.executeUpdate();
            }
            dataConnector.closeConnection();
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
        
    }
    
    
    
    public boolean runDelete(CrudFunction crudFunction,Map<String,Object> inputValues) {
        
        DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
        
        String content = crudFunction.getContent();
        
        try {
            try (PreparedStatement preparedStatement = dataConnector.getConnection().prepareStatement(content)) {
                Map<String,CrudInput> inputs = crudFunction.getInputs();
                int index = 1;
                
                for (Map.Entry<String,CrudInput> entry : inputs.entrySet()) {
                    CrudInput inputObj = entry.getValue();
                    
                    if (inputObj.getDataType()==CrudInput.INTEGER_FIELD_TYPE) {
                        preparedStatement.setInt(index, Integer.parseInt((String)inputValues.get(inputObj.getInputName())));
                    } else if (inputObj.getDataType()==CrudInput.STRING_FIELD_TYPE) {
                        preparedStatement.setString(index, (String)inputValues.get(inputObj.getInputName()));
                    } else if (inputObj.getDataType()==CrudInput.TIMESTAMP_FIELD_TYPE) {
                        preparedStatement.setTimestamp(index, new Timestamp(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    } else if (inputObj.getDataType()==CrudInput.DATE_FIELD_TYPE) {
                        preparedStatement.setDate(index, new Date(Long.parseLong((String)inputValues.get(inputObj.getInputName()))));
                    }
                    index++;
                }
                
                
                preparedStatement.executeUpdate();
            }
            dataConnector.closeConnection();
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
        
    }  
    
    
    public Map<Integer, Object> getFieldsForCreateFromDatabase(String t_crudobject,String t_crudtable) {
                
                final Map<Integer, Object> fieldsMap = new LinkedHashMap<>();
               
                new ObjectLoader().get(t_crudobject)
                              .getCrudTable(t_crudtable)
                              .getFields()
                              .forEach(field -> {

                                    Map<String,Object> fieldOptions = new LinkedHashMap<>();

                                    fieldOptions.put("displayname", field.getDisplayName());

                                    if (field.isExternal() && field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldName());
                                            fieldOptions.put("datatype", "external");
                                            fieldOptions.put("values",getExternalGetField(field));


                                    } else if (field.isExternal() && !field.isExternalForCreate()) {

                                            fieldOptions.put("name", field.getFieldName());
                                            fieldOptions.put("datatype", field.getExternalFieldDataTypeAsString());

                                    } else {

                                            fieldOptions.put("name", field.getFieldName());
                                            fieldOptions.put("datatype", field.getDataTypeAsString());

                                            if (field.getDataType()==DataTypes.TYPE_ENUM) 
                                                fieldOptions.put("values",field.getEnumValues());                                    

                                    }                        
                                    fieldsMap.put(fieldsMap.size(), fieldOptions);
                        
                        
                });
                
                return fieldsMap;
        
    }

        public Map<String,String> getExternalGetField (CrudField field) {
        
        
                                    MachineDataSet machineDataSet;
                                    try {
                                        machineDataSet = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername())
                                                                                        .getTable(field.getExternalTable())
                                                                                        .select()
                                                                                            .getColumn(field.getExternalGetField())
                                                                                            .getColumn(field.getExternalJoinField())
                                                                                        .compile()
                                                                                        .executeSelect();
                                        
                                    } catch (SQLException | ClassNotFoundException ex) {
                                        Logger.getLogger(ObjectOperations.class.getName()).log(Level.SEVERE, null, ex);
                                        return null;
                                    }

                                    
                                    String externalGetFields = field.getExternalGetField();
                                    
                                    if (externalGetFields.indexOf(",") > 0) {
                                        
                                        StringJoiner stringJoiner_columns_alias = new StringJoiner("_");

                                        for (String clm : externalGetFields.split(","))
                                            stringJoiner_columns_alias.add(clm);

                                        externalGetFields = stringJoiner_columns_alias.toString();
                                    }
                                    
                                    Map<String,String> externalValues = new LinkedHashMap<>();

                                    while (machineDataSet.next()) {

                                        if (field.getExternalFieldDataType()==DataTypes.TYPE_STRING) {
                                            externalValues.put(String.valueOf(machineDataSet.getInteger(field.getExternalJoinField())),
                                                               machineDataSet.getString(externalGetFields));
                                        }
                                        
                                    }
                                    
                                    return externalValues;
        
    }
        
     public Map handleNetondoBlock (String returnValue) throws ParseException, IOException, MachineDataException, ClassNotFoundException, SQLException {

        
                Map result = new DataUtils().readJsonObjectFromString(returnValue);
                
                
                if (result.get("netondo") != null) {
                    Map netondo_block = (Map)result.get("netondo");
                    if (netondo_block.get("run") != null) {
                            Map runBlock = (Map)netondo_block.get("run");
                            String command_type = (String)runBlock.get("command_type");
                            String command = (String)runBlock.get("command");
                            
                            String commandMode = (String)runBlock.get("command_mode");
                            
                            if (commandMode != null) {
                                if (commandMode.equals("nowait")) {
                                    command_mode = COMMAND_MODE_NOWAIT;
                                }
                            }                             


                            if (command_type.equals("executable")) {
                                
                                if (command_mode == COMMAND_MODE_WAIT) {
                                     result = handleNetondoBlock(runExternalScript(command,runBlock.get("arguments")));
                                } else if (command_mode == COMMAND_MODE_NOWAIT) {

                                     Runnable runnable =  () -> { 
                                         try {  
                                             handleNetondoBlock(runExternalScript(command,runBlock.get("arguments")));
                                         } catch (IOException | ParseException | MachineDataException | ClassNotFoundException | SQLException ex) {
                                             Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
                                         }
                                     };

                                     new Thread(runnable).start();

                                }
                                
                            } else if (command_type.equals("sqlfunction")) {
                                
                                if (command_mode == COMMAND_MODE_WAIT) {                                
                                    result = handleNetondoBlock(runExternalFunction(command,runBlock.get("arguments")));
                                } else if (command_mode == COMMAND_MODE_NOWAIT) {

                                     Runnable runnable =  () -> { 
                                         try {
                                             handleNetondoBlock(runExternalFunction(command,runBlock.get("arguments")));
                                         } catch (IOException | ParseException | MachineDataException | ClassNotFoundException | SQLException ex) {
                                             Logger.getLogger(FunctionOperations.class.getName()).log(Level.SEVERE, null, ex);
                                         }
                                     };

                                     new Thread(runnable).start();

                                }
                            }
                            
                    }
                    result.remove("netondo");
                }
            
                
                return result;
      
    }
    
    public String runExternalFunction (String command,Object arguments) throws MachineDataException, ClassNotFoundException, SQLException, IOException {

            //int arg_size = 0;
            String sql_query = "select " + command;
            //List arg_list=null;
            //Map arg_obj=null;
            
                if (arguments != null) {

                    /*if (arguments instanceof List) {
                            arg_list = (List)arguments;
                            arg_size = arg_list.size();
                            final StringJoiner valuesQueryPart = new StringJoiner(",","(",")");
                            arg_list.forEach( (val) -> {valuesQueryPart.add("?");  });

                            sql_query = sql_query + valuesQueryPart.toString();                          
                    } else if (arguments instanceof Map) { */
                            //arg_obj = (Map)arguments;
                            //arg_size = 1;
                            sql_query = sql_query + "(?)";
                    //}  

                } else {
                    sql_query = sql_query + "()";
                }
        
            DataConnector dataConnector = new DataConnector(crudModule.getGlobalEnvs(),crudModule.getUsername());
            
            
            String returnValue;
            
            try (PreparedStatement preparedStatement = dataConnector.getConnection().prepareStatement(sql_query)) {

                    if (arguments instanceof List) {
                            /*int index=1;
                            while (index <= arg_size && arg_size != 0) {
                                preparedStatement.setString(index, (String)arg_list.get(index-1));
                                index++;
                            } */  
                            String arg_object_as_str = new DataUtils().listToJsonString((List)arguments);
                            preparedStatement.setString(1, arg_object_as_str);                            
                    } else if (arguments instanceof Map) { 
                            String arg_object_as_str = new DataUtils().mapToJsonString((Map)arguments);
                            preparedStatement.setString(1, arg_object_as_str);
                    }                 
                            
                    returnValue = (String)new ResultSetConverter().resultSetToMachineDataSet(preparedStatement.executeQuery())
                                                                  .getResultAsMap()
                                                                  .get(0)
                                                                  .entrySet()
                                                                  .iterator()
                                                                  .next()
                                                                  .getValue();                 
                    return returnValue;
                
                

            }
                



    }
    
    public String runExternalScript (String command,Object arguments) throws IOException {

            
             String executeCommand;
             
             if (command.charAt(0)=='/') {
                executeCommand = command;
             } else {
                executeCommand = crudModule.getGlobalEnvs().get("scripts_path") + "/" + command;
             }
             
             if (executeCommand.contains("$scripts_path$")) executeCommand = executeCommand.replace("$scripts_path$", (String) crudModule.getGlobalEnvs().get("scripts_path"));

                   for (Object arg : (List)arguments) {
                       
                           String arg_str = (String) arg;
                           if (arg_str != null) {
                            if (arg_str.contains("$scripts_path$")) arg_str = arg_str.replace("$scripts_path$", (String) crudModule.getGlobalEnvs().get("scripts_path"));
                            if (arg_str.contains("$documents_path$")) arg_str = arg_str.replace("$documents_path$", (String) crudModule.getGlobalEnvs().get("documents_path"));
                           }
                           executeCommand = executeCommand + " '" + arg_str + "'";

                   }

                   
                   NLogger.logger(NLogBlock.SCRIPTS, NLog.MESSAGE,"FunctionOperations","runExternalScript",crudModule.getUsername(), "CMD -> " + executeCommand);

                   
                   Process running = new ProcessBuilder("bash","-c",executeCommand).start();

                   String outputOfScript = "";
                   BufferedReader bufferedReader;
                   
                    try (InputStream inputStream = running.getInputStream()) {
                        
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1);
                        String line;
                        
                        while ((line = bufferedReader.readLine()) != null) {
                            
                            outputOfScript = outputOfScript + line;

                        }
                    }

                   NLogger.logger(NLogBlock.SCRIPTS, NLog.MESSAGE,"FunctionOperations","runExternalScript",crudModule.getUsername(), "RESULT -> " + outputOfScript);

                   bufferedReader.close();
                   return outputOfScript;                    
        
    }
    
    
    
    
}
