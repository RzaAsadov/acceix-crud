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

package org.acceix.frontend.helpers;


import java.util.LinkedHashMap;
import java.util.Map;


public class ButtonsHelper {
    
    
        public Map<String,Object> createButton (String name,String url,String color,boolean isConfirmNeeded, String icon, String container) {
            
                        Map<String,Object> buttonresp = new LinkedHashMap<>();
                        buttonresp.put("name", name);
                        buttonresp.put("url", url);
                        buttonresp.put("color", color);
                        buttonresp.put("askconfirm", isConfirmNeeded);
                        buttonresp.put("icon", icon);
                        buttonresp.put("container", container);
                        buttonresp.put("size", "medium");

                        return buttonresp;
                        
        }
        
    
        public Map<String,Object> createButton (String name,String url,String color,boolean isConfirmNeeded, String icon, String size, String container) {
            
                        Map<String,Object> buttonresp = new LinkedHashMap<>();
                        buttonresp.put("name", name);
                        buttonresp.put("url", url);
                        buttonresp.put("color", color);
                        buttonresp.put("askconfirm", isConfirmNeeded);
                        buttonresp.put("icon", icon);
                        buttonresp.put("container", container);
                        buttonresp.put("size", size);

                        return buttonresp;
                        
        }
        
        public Map<String,Object> createLink (String name,String url,String color,boolean isConfirmNeeded, String icon, String container) {
                        Map<String,Object> buttonresp = new LinkedHashMap<>();
                        buttonresp.put("name", name);
                        buttonresp.put("url", url);
                        buttonresp.put("color", color);
                        buttonresp.put("askconfirm", isConfirmNeeded);
                        buttonresp.put("icon", icon);
                        buttonresp.put("container", container);

                        return buttonresp;
        }
    
}
