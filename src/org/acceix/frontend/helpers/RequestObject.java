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

package org.acceix.frontend.helpers;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RequestObject {

    public static int REQUEST_DATA_TYPE_JSON = 1;
    public static int REQUEST_DATA_TYPE_HTTP = 2;
    public static int REQUEST_DATA_TYPE_EMPTY = 3;
    public static int REQUEST_DATA_TYPE_UNKNOWN = 4;
    public static int REQUEST_DATA_TYPE_UNREADABLE = 5;    
    public static int REQUEST_DATA_TYPE_UNPARSABLE = 6;
    public static int REQUEST_DATA_TYPE_NO_MANDATORY_KEYS = 7;

    
    private String module = "";    
    private String action = "";
    
    private List rawInput;

    private Map<String,Object> params = new LinkedHashMap<>();
    
    private String requestBody;
    
    int requestType = RequestObject.REQUEST_DATA_TYPE_UNKNOWN;

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestBody() {
        return requestBody;
    }
    
    public RequestObject(Map<String, Object> params) {
        this.params = params;
    }

    public RequestObject() {
    }    

    public Map<String, Object> getParams() {
        return params;
    }
    
    public void addParam(String paramName,String paramValue) {
        params.put(paramName, paramValue);
    }
    
    public void removeParam(String paramName) {
        params.remove(paramName);
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    @JsonAnySetter
    void setParam(String key, Object value) {
        params.put(key, value);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setRawInput(List rawInput) {
        this.rawInput = rawInput;
    }

    public List getRawInput() {
        return rawInput;
    }
    
    
        
    
}
