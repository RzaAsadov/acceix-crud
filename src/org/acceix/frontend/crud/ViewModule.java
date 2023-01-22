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


import org.acceix.frontend.helpers.ActionSettings;
import org.acceix.frontend.helpers.ModuleHelper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zrid
 */
public class ViewModule extends org.acceix.frontend.helpers.ModuleHelper {
    
     

    @Override
    public void construct() {
        
        setModuleName("view");
        addAction(new ActionSettings("function", false, this::function));
        

    }

    @Override
    public ModuleHelper getInstance() {
       return new ViewModule();
    }

    
    

    public void function() {
        

                var viewActionPath = getRequestObject().getAction();
                
                //System.out.println("View is:" + viewActionPath);
                
                var inputParams = getRequestObject().getParams();
                
                
                // Pass all inputs to page
                inputParams.forEach( (key,value) -> {
                    addToDataModel(key, value);
                });
            

                    addToDataModel("message","function executed!");
                    addToDataModel("result", "success");
                    
                    try {  

                        
                        
                        renderData(viewActionPath);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ViewModule.class.getName()).log(Level.SEVERE, null, ex);
                    }

          

        
        
    }
    
    
    

  
    
    
}
