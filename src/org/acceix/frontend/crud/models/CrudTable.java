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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.acceix.logger.NLogger;
import org.acceix.logger.NLog;


public class CrudTable {
    
    
        public static int ORDER_BY_DESC=1;
        public static int ORDER_BY_ASC=2;
    
        private final Map<String,CrudField> fields = new LinkedHashMap<>();
        
        private final Map<String,CrudFilterField> readFilters = new LinkedHashMap<>();
        
        private List<String> orderByList = new LinkedList<>();
        
        private String idFieldName = "id";
        
        private String listIdField="";
        
        private int idFieldDataType;
        
        private String readLimit = "";
        
        private String addQuery = "";
        
        private int orderByDirection;
        
        private String name;

        public void setAddQuery(String addQuery) {
            this.addQuery = addQuery;
        }

        public String getAddQuery() {
            return addQuery;
        }

        public void setListIdField(String listIdFieldName) {
            this.listIdField = listIdFieldName.toLowerCase();
        }

        public String getListIdField() {
            return listIdField;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setOrderByDirection(int orderByDirection) {
            this.orderByDirection = orderByDirection;
        }

        public int getOrderByDirection() {
            return orderByDirection;
        }

        
        public void addOrderbyField(String fieldName,int orderId) {
            orderByList.add(fieldName);
        }

        public void setOrderByList(List<String> orderByList) {
            this.orderByList = orderByList;
        }

        
        
        public List<String> getOrderByFields () {
                return orderByList;
        }

        public void setIdFieldName(String idFieldName) {
            
            this.idFieldName = idFieldName.toLowerCase();
        }
        
        

        public String getIdFieldName() {
            return idFieldName;
        }

        public void setIdFieldDataType(int idFieldDataType) {
            this.idFieldDataType = idFieldDataType;
        }

        public int getIdFieldDataType() {
            return idFieldDataType;
        }



        public void setReadLimit(String readLimit) {
            this.readLimit = readLimit;
        }

        public String getReadLimit() {
            return readLimit;
        }


        
        public void addField (CrudField field) {
                fields.put(field.getFieldKey(), field);

        }

        public CrudField getField(String name) {
                
                if (fields.get(name)==null) {
                    CrudField crudField = getFields().stream()
                               .filter(item -> item.getFieldName().equals(name))
                               .findFirst().orElse(null);
                    return crudField;
                    
                } else {
                    return fields.get(name);
                }
        }
        
        public void addFilterForRead (CrudFilterField filterField) {
            readFilters.put(filterField.getFilterId(), filterField);
        } 
        
        public List<String> getFieldList() { 
            
            List<String> resultKeys = new ArrayList<>();
            fields.keySet().forEach((key) -> {
                resultKeys.add(fields.get(key).getFieldKey());
            });
            
            return resultKeys;
        }
        
        public Collection<CrudField> getFields() {
            return  fields.values();
            
        }
        
        public List<CrudField> getViewableFields() {
            List<CrudField> ret_fields = new LinkedList<>();
            fields.values().forEach( field -> {
                if (field.isViewable())
                    ret_fields.add(field);
            });
            return ret_fields;
            
        }   
        
        public List<CrudField> getListFields() {
            List<CrudField> ret_fields = new LinkedList<>();
            fields.values().forEach( field -> {
                if (field.isListData())
                    ret_fields.add(field);
            });
            return ret_fields;
            
        }         
        
        public Collection<CrudFilterField> getReadFilterFields() {
            return readFilters.values();
        }
    
}
