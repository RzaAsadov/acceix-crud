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

package org.acceix.frontend.crud.loaders;

import org.acceix.frontend.crud.models.CrudElemental;
import org.acceix.frontend.crud.models.CrudScript;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.acceix.frontend.crud.interfaces.Container;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;

/**
 *
 * @author zrid
 */
public class ScriptLoader implements Container<CrudScript> {

    
    private static Map<String,CrudScript> containerMap = new LinkedHashMap<>();    

    
    @Override
    public void add (CrudScript crudElement) {
        containerMap.put(crudElement.getName(), crudElement);   
    }
    
    @Override
    public CrudScript get (String name) {
                return containerMap.get(name);
    }
     
    @Override
    public List<CrudScript> getList() {
        
                CrudScript[] scriptArray = new CrudScript[containerMap.size()];
                
                int index=0;
                for (Map.Entry<String,CrudScript> entry : containerMap.entrySet()) {
                    scriptArray[index] = entry.getValue();
                    index++;
                }

                
                return Arrays.asList(scriptArray);

    }    
 
    
    @Override
    public void load(File file) {
        


        NLogger.logger(NLogBlock.SCRIPTS,NLog.MESSAGE,"ScriptLoader","load","system","Parsed script file: " + file);
                    
            if (file==null) 
                NLogger.logger(NLogBlock.SCRIPTS,NLog.ERROR,"ScriptLoader","load","system", "Unable to load script file  , it is NULL !");


            CrudScript crudScript = new CrudScript();

                if (file.exists()) {
                    if (file.isFile()) {
                        if (file.canRead()) {
                            crudScript.setName(file.getAbsolutePath());
                        } else {
                            NLogger.logger(NLogBlock.SCRIPTS,NLog.ERROR,"ScriptLoader","load","system", "Unable to load script file \"" + file.getName() + "\" , it is not readable (permissions ?) !");
                        }
                    } else {
                        NLogger.logger(NLogBlock.SCRIPTS,NLog.ERROR,"ScriptLoader","load","system", "Unable to load script ftle \"" + file.getName() + "\" , it is not file !");
                    }
                } else {
                    NLogger.logger(NLogBlock.SCRIPTS,NLog.ERROR,"ScriptLoader","load","system", "Unable to load script file \"" + file.getName() + "\" , it is not exists !");
                }

            crudScript.setFilepath(file.getAbsolutePath());
            crudScript.setTimeModified(file.lastModified());        
        


       containerMap.put(crudScript.getName(), crudScript);
                    

      
    } 
    
    @Override
     public void loadAll(String path) {
        
        
            var script_path = new File(path);
            
            if (script_path.exists() && script_path.isDirectory()) {
                
                File[] files = script_path.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

                        
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }                

                
            } else {
                NLogger.logger(NLogBlock.SCRIPTS,NLog.MESSAGE,"ScriptLoader","loadAll","system","No script folder on path: " + path);
            }
            
    }

    @Override
    public void unload (String scriptname) {
        containerMap.remove(scriptname);   
    }    
    
    @Override
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }      
    
    
}
