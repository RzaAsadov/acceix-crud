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

import org.acceix.frontend.crud.models.CrudDbView;
import org.acceix.frontend.database.AdminFunctions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.acceix.ndatabaseclient.exceptions.MachineDataException;
import org.acceix.frontend.crud.interfaces.Container;
import org.acceix.frontend.helpers.LoaderHelper;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;

/**
 *
 * @author zrid
 */
public class DbViewsLoader extends LoaderHelper implements Container<CrudDbView> {
    
    private static Map<String,CrudDbView> containerMap = new LinkedHashMap<>();    

    
    private static Map<String,Object> ENVS;
    
    private String last_error = "";
    
    public static void setGlobalEnvs(Map<String,Object> envs) {
        ENVS = envs;
    }

    public String getLast_error() {
        return last_error;
    }
    
    
    @Override
    public void load (File dbviewFile) {
        
        last_error = "";
        
        if (dbviewFile==null) {
            NLogger.logger(NLogBlock.DBVIEW,NLog.ERROR,"DbViewLoader","load","system", "Unable to load Stored DB file  , it is NULL !");
        }
        
        CrudDbView crudDbView = new CrudDbView();
        

        
        if (dbviewFile.exists()) {
            if (dbviewFile.isFile()) {
                if (dbviewFile.canRead()) {
                    
                    NLogger.logger(NLogBlock.DBVIEW,NLog.MESSAGE,"DbViewLoader","load","system","Loading dbview file ->" + dbviewFile.getName());
                    
                    crudDbView.setFilepath(dbviewFile.getAbsolutePath());
                    crudDbView.setTimeModified(dbviewFile.lastModified());
                    
                    StringBuilder contentBuilder = new StringBuilder();
                    try (Stream<String> stream = Files.lines( Paths.get(dbviewFile.getPath()), StandardCharsets.UTF_8)) {
                        stream.forEach(s -> contentBuilder.append(s).append("\n"));
                    } catch (IOException e) {
                       NLogger.logger(NLogBlock.DBVIEW,NLog.ERROR,"DbViewLoader","load","system","Unable to load Stored DB file \"" + dbviewFile.getName() + "\" , Exception message: " + e.getMessage());
                    }
                    
                    crudDbView.setName(dbviewFile.getName().split("\\.")[0]);
                    crudDbView.setContent(contentBuilder.toString());
                } else {
                    NLogger.logger(NLogBlock.DBVIEW, NLog.ERROR,"DbViewLoader","load","system", "Unable to load Stored DB file \"" + dbviewFile.getName() + "\" , it is not readable (permissions ?) !");
                }
            } else {
                NLogger.logger(NLogBlock.DBVIEW, NLog.ERROR,"DbViewLoader","load","system", "Unable to load Stored DB file \"" + dbviewFile.getName() + "\" , it is not file !");
            }
        } else {
            NLogger.logger(NLogBlock.DBVIEW, NLog.ERROR,"DbViewLoader","load","system", "Unable to load Stored DB file \"" + dbviewFile.getName() + "\" , it is not exists !");
        }
        
       containerMap.put(crudDbView.getName(), crudDbView);
        
        AdminFunctions databaseAdminFunctions = new AdminFunctions(ENVS,"system");

        if (!crudDbView.getContent().isEmpty()) {
  
            try {
                databaseAdminFunctions.executeStatement(crudDbView.getContent());
            } catch (MachineDataException | ClassNotFoundException | SQLException ex) {
                last_error = ex.getMessage();
                Logger.getLogger(DbViewsLoader.class.getName()).log(Level.SEVERE, null, ex);
            }

        }       

        
    }
    
    @Override
    public void add (CrudDbView crudDbView) {
        
            containerMap.put(crudDbView.getName(), crudDbView);

                    try {
                        new AdminFunctions(ENVS,"system").executeStatement(crudDbView.getContent());
                        
                    } catch (MachineDataException | ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }        
    }    
    
    @Override
    public  CrudDbView get (String dbview) {
                return containerMap.get(dbview);
    }
    
    @Override
    public  List<CrudDbView> getList() {
        
                CrudDbView[] db_view = new CrudDbView[containerMap.size()];
                
                int index=0;
                for (Map.Entry<String,CrudDbView> entry : containerMap.entrySet()) {
                    db_view[index] = entry.getValue();
                    index++;
                }
                

                Arrays.sort(db_view, Comparator.comparingLong(CrudDbView::getTimeModified).reversed());        
                
                
                return Arrays.asList(db_view);
                
    }    
 

    
    @Override
     public void loadAll(String path) {
        
        
            var dbStoredPath = new File(path);
            
            if (dbStoredPath.exists() && dbStoredPath.isDirectory()) {
                
                File[] files = dbStoredPath.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }                
                

                
            } else {
                NLogger.logger(NLogBlock.DBVIEW,NLog.ERROR,"DbViewLoader","load","system","No dbview folder on path: " + ENVS.get("dbview_path"));
            }
            
    }          

    public void unload (String objname) {
        containerMap.remove(objname);   
    }    
    
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }        



}
