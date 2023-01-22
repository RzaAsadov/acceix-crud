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

import org.acceix.frontend.crud.models.CrudDbEvent;
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
public class DbEventsLoader extends LoaderHelper implements Container<CrudDbEvent> {
    
    private static Map<String,CrudDbEvent> containerMap = new LinkedHashMap<>();    

    
    private static Map<String,Object> ENVS;
    
    private String last_error = "";
    
    public static void setGlobalEnvs(Map<String,Object> envs) {
        ENVS = envs;
    }

    public String getLast_error() {
        return last_error;
    }
    
    
    @Override
    public void load (File dbeventFile) {
        
        last_error = "";
        
        if (dbeventFile==null) {
            NLogger.logger(NLogBlock.DBEVENT,NLog.ERROR,"DbEventsLoader","load","system", "Unable to load Stored DB file  , it is NULL !");
        }
        
        CrudDbEvent crudDbEvent = new CrudDbEvent();
        

        
        if (dbeventFile.exists()) {
            if (dbeventFile.isFile()) {
                if (dbeventFile.canRead()) {
                    
                    NLogger.logger(NLogBlock.DBEVENT,NLog.MESSAGE,"DbEventsLoader","load","system","Loading dbevent file ->" + dbeventFile.getName());
                    
                    crudDbEvent.setFilepath(dbeventFile.getAbsolutePath());
                    crudDbEvent.setTimeModified(dbeventFile.lastModified());
                    
                    StringBuilder contentBuilder = new StringBuilder();
                    try (Stream<String> stream = Files.lines( Paths.get(dbeventFile.getPath()), StandardCharsets.UTF_8)) {
                        stream.forEach(s -> contentBuilder.append(s).append("\n"));
                    } catch (IOException e) {
                       NLogger.logger(NLogBlock.DBEVENT,NLog.ERROR,"DbEventsLoader","load","system","Unable to load Stored DB file \"" + dbeventFile.getName() + "\" , Exception message: " + e.getMessage());
                    }
                    
                    crudDbEvent.setName(dbeventFile.getName().split("\\.")[0]);
                    crudDbEvent.setContent(contentBuilder.toString());
                } else {
                    NLogger.logger(NLogBlock.DBEVENT, NLog.ERROR,"DbEventsLoader","load","system", "Unable to load Stored DB file \"" + dbeventFile.getName() + "\" , it is not readable (permissions ?) !");
                }
            } else {
                NLogger.logger(NLogBlock.DBEVENT, NLog.ERROR,"DbEventsLoader","load","system", "Unable to load Stored DB file \"" + dbeventFile.getName() + "\" , it is not file !");
            }
        } else {
            NLogger.logger(NLogBlock.DBEVENT, NLog.ERROR,"DbEventsLoader","load","system", "Unable to load Stored DB file \"" + dbeventFile.getName() + "\" , it is not exists !");
        }
        
       containerMap.put(crudDbEvent.getName(), crudDbEvent);
        
        AdminFunctions databaseAdminFunctions = new AdminFunctions(ENVS,"system");

        if (!crudDbEvent.getContent().isEmpty()) {
  
            try {
                databaseAdminFunctions.executeStatement(crudDbEvent.getContent());
            } catch (MachineDataException | ClassNotFoundException | SQLException ex) {
                last_error = ex.getMessage();
                Logger.getLogger(DbEventsLoader.class.getName()).log(Level.SEVERE, null, ex);
            }

        }       

        
    }
    
    @Override
    public void add (CrudDbEvent crudDbEvent) {
        
            containerMap.put(crudDbEvent.getName(), crudDbEvent);

                    try {
                        new AdminFunctions(ENVS,"system").executeStatement(crudDbEvent.getContent());
                        
                    } catch (MachineDataException | ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }        
    }    
    
    @Override
    public  CrudDbEvent get (String dbevent) {
                return containerMap.get(dbevent);
    }
    
    @Override
    public  List<CrudDbEvent> getList() {
        
                CrudDbEvent[] db_event = new CrudDbEvent[containerMap.size()];
                
                int index=0;
                for (Map.Entry<String,CrudDbEvent> entry : containerMap.entrySet()) {
                    db_event[index] = entry.getValue();
                    index++;
                }
                

                Arrays.sort(db_event, Comparator.comparingLong(CrudDbEvent::getTimeModified).reversed());        
                
                
                return Arrays.asList(db_event);
                
    }    
 

    
    @Override
     public void loadAll(String path) {
        
        
            var dbEventPath = new File(path);
            
            if (dbEventPath.exists() && dbEventPath.isDirectory()) {
                
                File[] files = dbEventPath.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }                
                

                
            } else {
                NLogger.logger(NLogBlock.DBEVENT,NLog.ERROR,"DbEventsLoader","load","system","No dbevent folder on path: " + ENVS.get("dbevent_path"));
            }
            
    }          

    public void unload (String objname) {
        containerMap.remove(objname);   
    }    
    
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }        



}
