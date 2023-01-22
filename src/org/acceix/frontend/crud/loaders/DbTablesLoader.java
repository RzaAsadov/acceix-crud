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

import org.acceix.frontend.crud.models.CrudDbTable;
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
public class DbTablesLoader extends LoaderHelper implements Container<CrudDbTable> {
    
    private static Map<String,CrudDbTable> containerMap = new LinkedHashMap<>();    

    
    private static Map<String,Object> ENVS;
    
    private String last_error = "";
    
    public static void setGlobalEnvs(Map<String,Object> envs) {
        ENVS = envs;
    }

    public String getLast_error() {
        return last_error;
    }
    
    
    @Override
    public void load (File dbtableFile) {
        
        last_error = "";
        
        if (dbtableFile==null) {
            NLogger.logger(NLogBlock.DBTABLE,NLog.ERROR,"DbTablesLoader","load","system", "Unable to load DB table file  !");
        }
        
        CrudDbTable crudDbTable = new CrudDbTable();
        

        
        if (dbtableFile.exists()) {
            if (dbtableFile.isFile()) {
                if (dbtableFile.canRead()) {
                    
                    NLogger.logger(NLogBlock.DBTABLE,NLog.MESSAGE,"DbTablesLoader","load","system","Loading dbtable file ->" + dbtableFile.getName());
                    
                    crudDbTable.setFilepath(dbtableFile.getAbsolutePath());
                    crudDbTable.setTimeModified(dbtableFile.lastModified());
                    
                    StringBuilder contentBuilder = new StringBuilder();
                    try (Stream<String> stream = Files.lines( Paths.get(dbtableFile.getPath()), StandardCharsets.UTF_8)) {
                        stream.forEach(s -> contentBuilder.append(s).append("\n"));
                    } catch (IOException e) {
                       NLogger.logger(NLogBlock.DBTABLE,NLog.ERROR,"DbTablesLoader","load","system","Unable to load DB table file \"" + dbtableFile.getName() + "\" , Exception message: " + e.getMessage());
                    }
                    
                    crudDbTable.setName(dbtableFile.getName().split("\\.")[0]);
                    crudDbTable.setContent(contentBuilder.toString());
                } else {
                    NLogger.logger(NLogBlock.DBTABLE, NLog.ERROR,"DbTablesLoader","load","system", "Unable to load DB ta file \"" + dbtableFile.getName() + "\" , it is not readable (permissions ?) !");
                }
            } else {
                NLogger.logger(NLogBlock.DBTABLE, NLog.ERROR,"DbTablesLoader","load","system", "Unable to load DB table file \"" + dbtableFile.getName() + "\" , it is not file !");
            }
        } else {
            NLogger.logger(NLogBlock.DBTABLE, NLog.ERROR,"DbTablesLoader","load","system", "Unable to load DB table file \"" + dbtableFile.getName() + "\" , it is not exists !");
        }
        
       containerMap.put(crudDbTable.getName(), crudDbTable);
        
        AdminFunctions databaseAdminFunctions = new AdminFunctions(ENVS,"system");

        if (!crudDbTable.getContent().isEmpty()) {
  
            try {
                databaseAdminFunctions.executeStatement(crudDbTable.getContent());
            } catch (MachineDataException | ClassNotFoundException | SQLException ex) {
                last_error = ex.getMessage();
                Logger.getLogger(DbTablesLoader.class.getName()).log(Level.SEVERE, null, ex);
            }

        }       

        
    }
    
    @Override
    public void add (CrudDbTable crudDbTable) {
        
            containerMap.put(crudDbTable.getName(), crudDbTable);

                    try {
                        new AdminFunctions(ENVS,"system").executeStatement(crudDbTable.getContent());
                        
                    } catch (MachineDataException | ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }        
    }    
    
    @Override
    public  CrudDbTable get (String dbtable) {
                return containerMap.get(dbtable);
    }
    
    @Override
    public  List<CrudDbTable> getList() {
        
                CrudDbTable[] db_table = new CrudDbTable[containerMap.size()];
                
                int index=0;
                for (Map.Entry<String,CrudDbTable> entry : containerMap.entrySet()) {
                    db_table[index] = entry.getValue();
                    index++;
                }
                

                Arrays.sort(db_table, Comparator.comparingLong(CrudDbTable::getTimeModified).reversed());        
                
                
                return Arrays.asList(db_table);
                
    }    
 

    
    @Override
     public void loadAll(String path) {
        
        
            var dbTablePath = new File(path);
            
            if (dbTablePath.exists() && dbTablePath.isDirectory()) {
                
                File[] files = dbTablePath.listFiles();
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                
                for (File objfile : files) {
                    
                        if(objfile.isDirectory()){
                            loadAll(objfile.getAbsolutePath());
                        } else {
                            load(objfile);
                        }                    
                        
                }                
                

                
            } else {
                NLogger.logger(NLogBlock.DBTABLE,NLog.ERROR,"DbTablesLoader","load","system","No dbtable folder on path: " + path);
            }
            
    }          

    public void unload (String objname) {
        containerMap.remove(objname);   
    }    
    
    public void reset() {
        containerMap = new LinkedHashMap<>();
    }        



}
