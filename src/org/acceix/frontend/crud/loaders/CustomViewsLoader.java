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
import org.acceix.frontend.crud.models.CrudView;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;
import org.acceix.frontend.crud.interfaces.Container;

/**
 *
 * @author zrid
 */
public class CustomViewsLoader implements Container {
    
    
    private static Map<String,CrudView> containerMap = new LinkedHashMap<>();    
    

    
    @Override
    public void add (CrudElemental crudElement) {
        containerMap.put(crudElement.getName(), (CrudView)crudElement);   
    }
    
    @Override
    public CrudElemental get (String name) {
                return containerMap.get(name);
    }
    
    @Override
    public List<CrudView> getList() {

            CrudView[] array = new CrudView[containerMap.size()];

            int index=0;
            for (Map.Entry<String,CrudView> entry : containerMap.entrySet()) {
                array[index] = entry.getValue();
                index++;
            }

            return Arrays.asList(array);

    }
 
    
    @Override
    public  void load(File file) {
        
        
        if (file==null) {
            NLogger.logger(NLogBlock.VIEW,NLog.ERROR,"CustomViewsLoader","load","system", "Unable to load view file  , it is NULL !");
            return;
        }

        
        CrudView crudView = new CrudView();
        
        crudView.setFilepath(file.getAbsolutePath());
        crudView.setTimeModified(file.lastModified());

        
        if (file.exists()) {
            if (file.isFile()) {
                if (file.canRead()) {
                    crudView.setName(file.getAbsolutePath());
                } else {
                    NLogger.logger(NLogBlock.VIEW,NLog.ERROR,"CustomViewsLoader","load","system", "Unable to load view file \"" + file.getName() + "\" , it is not readable (permissions ?) !");
                    return;
                }
            } else {
                NLogger.logger(NLogBlock.VIEW,NLog.ERROR,"CustomViewsLoader","load","system", "Unable to load view ftle \"" + file.getName() + "\" , it is not file !");
                return;
            }
        } else {
            NLogger.logger(NLogBlock.VIEW,NLog.ERROR,"CustomViewsLoader","load","system", "Unable to load view file \"" + file.getName() + "\" , it is not exists !");
            return;
        }
        
        containerMap.put(crudView.getName(), crudView);

      
    } 
    
    @Override
    public void loadAll(String path) {
        
        
            var view_pth = new File(path);
            
            if (view_pth.exists() && view_pth.isDirectory()) {
                
                File[] files = view_pth.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

                        
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }                

                
            } else {
                NLogger.logger(NLogBlock.VIEW,NLog.MESSAGE,"CustomViewsLoader","loadAll","system","No view folder on path: " + path);
            }
            
    }

    @Override
    public void unload (String name) {
        containerMap.remove(name);   
    }    

    
    @Override
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }    



        
    
    
}
