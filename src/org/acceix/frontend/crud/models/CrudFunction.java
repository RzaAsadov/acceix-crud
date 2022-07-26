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

import java.util.LinkedHashMap;
import java.util.Map;


public class CrudFunction extends  CrudElemental {
    
    public static int FUNCTION_TYPE_EXECUTE = 1;
    public static int FUNCTION_TYPE_SQL = 2;
    public static int FUNCTION_TYPE_PROCEDURE = 3;
    public static int FUNCTION_TYPE_UNKNWON = 4;
    
    public static int OPERATION_TYPE_CREATE = 1;
    public static int OPERATION_TYPE_READ = 2;
    public static int OPERATION_TYPE_UPDATE = 3;
    public static int OPERATION_TYPE_DELETE = 4;
    public static int OPERATION_TYPE_FUNCTION = 5;
    public static int OPERATION_TYPE_PROCEDURE = 6;
    public static int OPERATION_TYPE_EXECUTABLE = 7;
    
    private String filepath = "";
    
    private long timeModified;    
    
    private boolean requireAuth = true;
    
    private int user_id = -1;
    private String domain = "unknown";
    
    private String templateForRun;
    private String templateForResult;
    
    private int objectType = 0;
    
    private int operationType;
    
    private String title,roleRun;
    
    private final Map<String,CrudInput> inputs = new LinkedHashMap<>();
    
    private final Map<String,String> function = new LinkedHashMap<>();
    private Object Map;

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getDomain() {
        return domain;
    }
    
    

    public void setTemplateForRun(String templateForRun) {
        this.templateForRun = templateForRun;
    }

    public String getTemplateForRun() {
        return templateForRun;
    }

    public void setTemplateForResult(String templateForResult) {
        this.templateForResult = templateForResult;
    }

    public String getTemplateForResult() {
        return templateForResult;
    }
    


    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }

    public boolean isRequireAuth() {
        return requireAuth;
    }

    public void setRoleRun(String roleRun) {
        this.roleRun = roleRun;
    }

    public String getRoleRun() {
        return roleRun;
    }
    
    

    
    public void setOperationType(String operationType) {

        switch (operationType) {
            case "create":
                this.operationType = OPERATION_TYPE_CREATE;
                break;
            case "read":
                this.operationType = OPERATION_TYPE_READ;
                break;
            case "update":
                this.operationType = OPERATION_TYPE_UPDATE;
                break;
            case "delete":
                this.operationType = OPERATION_TYPE_DELETE;
                break;
            case "function":
                this.operationType = OPERATION_TYPE_FUNCTION;
                break;
            case "procedure":
                this.operationType = OPERATION_TYPE_PROCEDURE;
                break;
            case "executable":
                this.operationType = OPERATION_TYPE_EXECUTABLE;
                break;                
            default:
                break;
        }
        
    }

    public int getOperationType() {
        return operationType;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    

    public void setObjectType(String objectTypeAsString) {
        switch (objectTypeAsString) {
            case "functionSql":
                objectType = FUNCTION_TYPE_SQL;
                break;
            case "functionProcedure":
                objectType = FUNCTION_TYPE_PROCEDURE;
                break;
            case "functionExecute":
                objectType = FUNCTION_TYPE_EXECUTE;
                break;
            default:
                objectType = FUNCTION_TYPE_UNKNWON;
                break;
        }

    }


    public int getObjectType() {
        return objectType;
    }      
    
    public String getOperationTypeAsString() {
        if (getOperationType()==OPERATION_TYPE_CREATE) {
            return "create";
        } else if (getOperationType()==OPERATION_TYPE_READ) {
            return "read";
        } else if (getOperationType()==OPERATION_TYPE_UPDATE) {
            return "update";
        } else if (getOperationType()==OPERATION_TYPE_DELETE) {
            return "update";
        } else if (getOperationType()==OPERATION_TYPE_FUNCTION) {
            return "function";
        } else if (getOperationType()==OPERATION_TYPE_PROCEDURE) {
            return "procedure";
        } else if (getOperationType()==OPERATION_TYPE_EXECUTABLE) {
            return "procedure";
        } else {
            return "unknown";
        }
    }
    
    public void addInput(CrudInput crudInput) {
        inputs.put(crudInput.getInputName(), crudInput);
    }

    public Map<String, CrudInput> getInputs() {

        return inputs;
    }
    
    
    public void setType (String type) {
        function.put("type", type);
    }

    
    
}


