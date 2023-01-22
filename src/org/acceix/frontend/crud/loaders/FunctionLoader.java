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

package org.acceix.frontend.crud.loaders;

import org.acceix.frontend.crud.models.CrudFunction;
import org.acceix.frontend.crud.models.CrudInput;
import org.acceix.frontend.web.commons.DataUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.frontend.crud.interfaces.Container;
import org.acceix.frontend.helpers.LoaderHelper;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author zrid
 */
public class FunctionLoader extends LoaderHelper implements Container<CrudFunction> {
    
    private static Map<String,CrudFunction> containerMap = new LinkedHashMap<>();
    
    
    private static Map<String,Object> ENVS;

    public static void setGlobalEnvs(Map<String, Object> ENVS) {
        FunctionLoader.ENVS = ENVS;
    }
    
    @Override
    public void add (CrudFunction nCrudFunction) {
        containerMap.put(nCrudFunction.getName(), nCrudFunction);
    }
    
    @Override
    public CrudFunction get(String func) {
                return containerMap.get(func);
    }  
    
    public CrudFunction get(int obj) {
        return (CrudFunction)containerMap.get((String)containerMap.keySet().toArray()[obj]);
    }
    
    @Override
    public List<CrudFunction> getList() {

            CrudFunction[] system_objects = new CrudFunction[containerMap.size()];

            int index=0;
            for (Map.Entry<String,CrudFunction> entry : containerMap.entrySet()) {
                system_objects[index] = entry.getValue();
                index++;
            }



            Arrays.sort(system_objects, Comparator.comparingLong(CrudFunction::getTimeModified).reversed());        


            return Arrays.asList(system_objects);

    }
    
    
    @Override
    public void load(File functionFile) {
        

            if (functionFile.exists() && functionFile.canRead()) {

                try {

                    NLogger.logger(NLogBlock.FUNCTIONS,NLog.MESSAGE,"DbStoredLoader","load","system","Parsed function file: " + functionFile);

                    var crudFunction = FunctionLoader.readFunction(
                                                (Map) new DataUtils().readJsonObjectFromString(
                                                            new String (Files.readAllBytes(
                                                            Paths.get(functionFile.toURI())))),functionFile);

                    containerMap.put(crudFunction.getName(),crudFunction);

                } catch (IOException | ParseException ex) {
                    ex.addSuppressed(new Throwable("On file " + functionFile));
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }

            }
      
    }    
    
    @Override
    public void loadAll(String path) {
    
        
            var functionsPath = new File(path);
            
            if (functionsPath.exists() && functionsPath.isDirectory()) {
                
                File[] files = functionsPath.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }                

                
            } else {
                NLogger.logger(NLogBlock.FUNCTIONS,NLog.ERROR,"FunctionLoader","loadAll","system","No objects folder on path: " + ENVS.get("functions_path"));
            }
            
    }    
    

    @Override
    public void unload (String objname) {
        containerMap.remove(objname);   
    }    
    
    @Override
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }        
    

           

    public static CrudFunction readFunction (Map jSONObject,File objfile) {
        
        CrudFunction crudFunction = new CrudFunction();
        crudFunction.setName((String)jSONObject.get("name"));        
        
        crudFunction.setObjectType((String)jSONObject.getOrDefault("objectType","functionSQL"));
        crudFunction.setOperationType((String)jSONObject.getOrDefault("operationType",""));

        if (objfile != null) {
            crudFunction.setFilepath(objfile.getAbsolutePath());
            crudFunction.setTimeModified(objfile.lastModified());
        } else {
            crudFunction.setFilepath(null);
            crudFunction.setTimeModified(0);
        }
        

        crudFunction.setTitle((String)jSONObject.get("title"));
        
        crudFunction.setRequireAuth((boolean)jSONObject.getOrDefault("requireAuth",Boolean.TRUE));
        crudFunction.setTemplateForRun((String)jSONObject.getOrDefault("templateForRun","/defaultTemplates/createFunctionData"));
        crudFunction.setTemplateForResult((String)jSONObject.getOrDefault("templateForResult","/defaultTemplates/showFunctionData"));

        
        crudFunction.setRoleRun((String)jSONObject.getOrDefault("roleRun",""));
        
        
        NLogger.logger(NLogBlock.FUNCTIONS,NLog.MESSAGE,"FunctionLoader","readFunction","system","Found function -> [" + crudFunction.getName() + "]");
        
        // Get Table data
        
        Map JSONObject_inputs =  (Map) jSONObject.get("inputs");
        
        if (JSONObject_inputs != null) {
        
            JSONObject_inputs.keySet().forEach((var inputkey) -> {
 
                NLogger.logger(NLogBlock.FUNCTIONS,NLog.MESSAGE,"FunctionLoader","readFunction","system","In function [" + crudFunction.getName() + "] found input \"" + inputkey + "\"");                

                        crudFunction.addInput(
                                                readInputField((String)inputkey,
                                                    (Map)JSONObject_inputs.get(inputkey)
                                                )
                                              );
            });
            
        }
        // Get metadata fields
            
        Map JSONObject_function =  (Map) jSONObject.get("function");
        
        if (JSONObject_function != null) {
        
            String content = (String)JSONObject_function.get("content");
            if (content != null) {
                crudFunction.setContent(content);
            }
        }
        
        return crudFunction;
        
        
        
        
    }


    
    private static CrudInput readInputField (String fieldname, Map table_field) {
        
                    CrudInput nCrudInput = new CrudInput();

                    nCrudInput.setInputName(fieldname);

                    nCrudInput.setDataType((String)table_field.getOrDefault("dataType","string"));
                    
                    nCrudInput.setTestValue(table_field.getOrDefault("testValue",null));
                    
                    nCrudInput.setFixedValue(table_field.getOrDefault("fixedValue",null));
                    
                    return nCrudInput;
        
    }

     
    
}
