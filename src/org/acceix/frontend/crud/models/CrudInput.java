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

import org.acceix.ndatabaseclient.DataTypes;


public class CrudInput {
    
        public static int UNKNOWN_FIELD_TYPE=-1;    
        
        public static int STRING_FIELD_TYPE=0;
        public static int INTEGER_FIELD_TYPE=1;
        public static int BOOL_FIELD_TYPE=2;
        public static int TIMESTAMP_FIELD_TYPE=3;        
        public static int JSON_FIELD_TYPE = 4;
        public static int ENUM_FIELD_TYPE = 5;
        public static int DATE_FIELD_TYPE = 6;
        public static int TIME_FIELD_TYPE = 7;
        public static int DATETIME_FIELD_TYPE = 8;  
        public static int LONG_FIELD_TYPE=9;        
        public static int FLOAT_FIELD_TYPE=10;
        public static int DOUBLE_FIELD_TYPE=11; 
        public static int TEXT_FIELD_TYPE=12;
        public static int PAYLOAD_TYPE=19;
        public static int FIXED_TYPE=20;
        
        private String inputName;        
        private int dataType;
        private Object testValue=null;
        private Object fixedValue=null;


        public String getInputName() {
            return inputName;
        }

        public void setInputName(String inputName) {
            this.inputName = inputName;
        }

        public int getDataType() {
            return dataType;
        }

        public void setTestValue(Object testValue) {
            this.testValue = testValue;
        }

        public Object getTestValue() {
            return testValue;
        }

        public void setFixedValue(Object fixedValue) {
            this.fixedValue = fixedValue;
        }

        public Object getFixedValue() {
            return fixedValue;
        }

        private void setDataType(int dataType) {
            this.dataType = dataType;
        }    

        public void setDataType(String dataType) {
                setDataType(new DataTypes().stringToDataType(dataType));
        }  
        
        
        
    
}
