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

package org.acceix.frontend.crud.models;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CrudObject extends CrudElemental {
    
        public static int OBJECT_TYPE_UNKNOWN = 0;
        public static int OBJECT_TYPE_TABLE = 1;


        public static int OPERATION_TYPE_CREATE = 1;
        public static int OPERATION_TYPE_READ = 2;
        public static int OPERATION_TYPE_UPDATE = 3;
        public static int OPERATION_TYPE_DELETE = 4;
        
        private int objectType,user_id=-1;
        private final List<Integer> operationTypes = new LinkedList<>();
        
        private final Map<String,CrudTable> crudTables = new LinkedHashMap<>();
        private final Map<String,Object> metadata = new LinkedHashMap<>();        

    
        private String title="",domain="";
        
        private Object defaultValue = null;

        private boolean creatable = false,editable = false,deletable = false,requireAuth = true;


        
        
        
        private String roleCreate,roleRead,roleUpdate,roleDelete;
        private String templateForCreate=null;
        private String templateForRead=null;
        private String templateForListRead=null;
        private String templateForUpdate=null;
        private String templateForDelete=null;
        private String templateForFilters=null;
        private String templateForFiles=null;
        private String templateForLocation=null;
        

        public void setObjectType(String objectTypeAsString) {
            if (objectTypeAsString.equals("table")) {
                objectType = OBJECT_TYPE_TABLE;
            } else {
                objectType = OBJECT_TYPE_UNKNOWN;
            }

        }

        public int getObjectType() {
            return objectType;
        }

        public void setOperationType(String operationTypesString) {
            if (operationTypesString.contains(",")) {
                String[] operTypes = operationTypesString.split(",");
                for (String operType : operTypes) {
                    switch (operType) {
                        case "create":
                            operationTypes.add(OPERATION_TYPE_CREATE);
                            break;
                        case "read":
                            operationTypes.add(OPERATION_TYPE_READ);
                            break;
                        case "update":
                            operationTypes.add(OPERATION_TYPE_UPDATE);
                            break;
                        case "delete":
                            operationTypes.add(OPERATION_TYPE_DELETE);
                            break;
                        default:
                            break;
                    }
                }
            } else {
                    switch (operationTypesString) {
                        case "create":
                            operationTypes.add(OPERATION_TYPE_CREATE);
                            break;
                        case "read":
                            operationTypes.add(OPERATION_TYPE_READ);
                            break;
                        case "update":
                            operationTypes.add(OPERATION_TYPE_UPDATE);
                            break;
                        case "delete":
                            operationTypes.add(OPERATION_TYPE_DELETE);
                            break;
                        default:
                            break;
                    }                
                
            }
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        
        
        public List<Integer> getOperationTypes() {
            return operationTypes;
        }


        public void setRoleCreate(String roleCreate) {
            this.roleCreate = roleCreate;
        }

        public void setRoleRead(String roleRead) {
            this.roleRead = roleRead;
        }

        public void setRoleUpdate(String roleUpdate) {
            this.roleUpdate = roleUpdate;
        }

        public void setRoleDelete(String roleDelete) {
            this.roleDelete = roleDelete;
        }

        public String getRoleCreate() {
            return roleCreate;
        }

        public String getRoleRead() {
            return roleRead;
        }

        public String getRoleUpdate() {
            return roleUpdate;
        }

        public String getRoleDelete() {
            return roleDelete;
        }

        public void setTemplateForCreate(String templateForCreate) {
            this.templateForCreate = templateForCreate;
        }

        public void setTemplateForRead(String templateForRead) {
            this.templateForRead = templateForRead;
        }

        public void setTemplateForUpdate(String templateForUpdate) {
            this.templateForUpdate = templateForUpdate;
        }

        public void setTemplateForDelete(String templateForDelete) {
            this.templateForDelete = templateForDelete;
        }

        public void setTemplateForFilters(String templateForFilters) {
            this.templateForFilters = templateForFilters;
        }

        public void setTemplateForListRead(String templateForListRead) {
            this.templateForListRead = templateForListRead;
        }

        public void setTemplateForFiles(String templateForFiles) {
            this.templateForFiles = templateForFiles;
        }

        public void setTemplateForLocation(String templateForLocation) {
            this.templateForLocation = templateForLocation;
        }



        public String getTemplateForFiles() {
            return templateForFiles;
        }

        public String getTemplateForLocation() {
            return templateForLocation;
        }


        public String getTemplateForListRead() {
            return templateForListRead;
        }


        public String getTemplateForCreate() {
            return templateForCreate;
        }

        public String getTemplateForRead() {
            return templateForRead;
        }

        public String getTemplateForUpdate() {
            return templateForUpdate;
        }

        public String getTemplateForDelete() {
            return templateForDelete;
        }

        public String getTemplateForFilters() {
            return templateForFilters;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return domain;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public int getUser_id() {
            return user_id;
        }



        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public boolean isCreatable() {
            return creatable;
        }

        public void setCreatable(boolean creatable) {
            this.creatable = creatable;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isDeletable() {
            return deletable;
        }

        public void setDeletable(boolean deletable) {
            this.deletable = deletable;
        }



        public boolean isRequireAuth() {
            return requireAuth;
        }

        public void setRequireAuth(boolean requireAuth) {
            this.requireAuth = requireAuth;
        }


        public CrudTable getCrudTable(String name) {
            return crudTables.getOrDefault(name, null);
        }

        public void addCrudTable(String table_name, CrudTable nCrudTable) {
            crudTables.put(table_name, nCrudTable);
        }

        public Map<String, CrudTable> getCrudTables() {
            return crudTables;
        }

        public CrudTable getDefaultCrudTable() {
            return crudTables.values().iterator().next();
        }

        public void addMetaData(String key,Object value) {
            metadata.put(key, value);
        }
        
        public Object getMetaData(String key) {
            return metadata.get(key);
        }
        
        public List<String> getMetaDataKeys() {
            return new LinkedList(metadata.keySet());
        }
    
}
