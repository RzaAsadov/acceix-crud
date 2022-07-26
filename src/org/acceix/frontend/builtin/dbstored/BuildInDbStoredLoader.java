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

package org.acceix.frontend.builtin.dbstored;

import org.acceix.frontend.builtin.objects.BuiltInObjectsLoader;
import org.acceix.frontend.crud.loaders.DbStoredLoader;
import org.acceix.frontend.crud.models.CrudDbStored;
import org.acceix.frontend.helpers.DirectoryLister;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author zrid
 */
public class BuildInDbStoredLoader {
    
    
    public void loadDbStored() {
        
        
                List<String> fileList = null;
                
                
                try {
                    fileList = DirectoryLister.getResourceListing(this.getClass(), 
                                                                  this.getClass()
                                                                      .getPackageName()
                                                                      .replace('.', '/'),".sql");
                } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(BuiltInObjectsLoader.class.getName()).log(Level.SEVERE, null, ex);
                }        
                
                
                fileList.stream().map((file) -> new BufferedReader(
                                                    new InputStreamReader(
                                                        this.getClass()
                                                            .getClassLoader()
                                                            .getResourceAsStream(file)))
                                                            .lines()
                                                            .collect(Collectors.joining("\n"))).forEachOrdered((result) -> {
                                                                
                                                                var dbstored_t = new CrudDbStored();
                                                                dbstored_t.setContent(result);
                                                                
                                                                new DbStoredLoader().add(dbstored_t);
                    
                                                            });                
        
    }
    
    
}
