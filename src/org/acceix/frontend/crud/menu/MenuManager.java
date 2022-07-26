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

package org.acceix.frontend.crud.menu;

import org.acceix.frontend.database.AdminFunctions;
import org.acceix.frontend.models.RoleModel;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.acceix.ndatabaseclient.MachineDataException;
import org.acceix.ndatabaseclient.MachineDataSet;
import org.acceix.ndatabaseclient.DataConnector;

/**
 *
 * @author zrid
 */
public class MenuManager {
    
    private final Map<String,Object> globalEnvs;
    
    private String username;

    public MenuManager(Map<String, Object> envs,String username) {
        this.globalEnvs = envs;
        this.username = username;
    }

    private Map<String, Object> getGlobalEnvs() {
        return globalEnvs;
    }

    public String getUsername() {
        return username;
    }
    
    
    
    
    public Map<Integer,Object> menu(List<String> roles) {
        

            
            try {
                
 

                    Map<Integer,String> allRolesMap = new LinkedHashMap<>();
                    
                    new AdminFunctions(globalEnvs,getUsername()).getAllRoleList().forEach( (RoleModel rolemodel) -> {
                        allRolesMap.put(rolemodel.getId(), rolemodel.getRolename());
                    });

            
                
                MachineDataSet machineDataSet_menu_categories = 
                                                new DataConnector(getGlobalEnvs(),getUsername())
                                                              .getTable("menu_categories")
                                                              .select()
                                                                  .getColumn("id")
                                                                  .getColumn("name")
                                                                  .getColumn("icon")
                                                              .compile()
                                                              .executeSelect();
                
                Map<Integer,Object> menuCategories = new LinkedHashMap<>();
                
                int category_number = 0;
                
                while (machineDataSet_menu_categories.next()) {
                    
                    Map<Object,Object> menuCategory = new LinkedHashMap<>();
                    
                    int menu_category_id = machineDataSet_menu_categories.getInteger("id");
                    String menu_category_name = machineDataSet_menu_categories.getString("name");
                    
                    
                    String menu_category_icon = machineDataSet_menu_categories.getString("icon");
                    
                    
                    menuCategory.put("menu_category_name", menu_category_name);
                    menuCategory.put("menu_category_icon", menu_category_icon);
                    
                    MachineDataSet machineDataSet_menu_items = new DataConnector(getGlobalEnvs(),getUsername()).getTable("menu_items")
                                                                        .select()
                                                                            .getColumn("name")
                                                                            .getColumn("role_id")
                                                                            .getColumn("link")
                                                                        .where()
                                                                            .eq("cat_id", menu_category_id)
                                                                        .compile()
                                                                        .executeSelect();
                                Map<Object,Object> menu_items = new LinkedHashMap<>(); 
                                
                                int item_number = 0;
                                
                                while (machineDataSet_menu_items.next()) {
                                    
                                    Map<Object,Object> menu_item = new LinkedHashMap<>();                    
                                    
                                    String menu_item_name = machineDataSet_menu_items.getString("name");
                                    int menu_item_role_id = machineDataSet_menu_items.getInteger("role_id");
                                    String menu_item_link = machineDataSet_menu_items.getString("link");
                                    
                                    boolean found = false;
                                    for (String roleAvialable : roles) {
                                        if (roleAvialable.equals(allRolesMap.get(menu_item_role_id))) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    
                                    if (!found) continue; // If menu role not avialable for user , ignore this menu field
                                    
                                    menu_item.put("menu_item_name", menu_item_name);
                                    menu_item.put("menu_item_link", menu_item_link);
                                    
                                    // Temporary solution
                                    Pattern p = Pattern.compile("obj=([A-Za-z0-9_ -]+)");
                                    Matcher n = p.matcher(menu_item_link);
                                    if (n.find()) {
                                        menu_item.put("menu_item_object_name", n.group(1));
                                    } else {
                                        menu_item.put("menu_item_object_name", "none");
                                    }
                                    
                                    menu_items.put(item_number, menu_item);
                                    item_number++;
                                }
                                
                                if (menu_items.size() > 0 ) {
                    
                                      menuCategory.put("menu_items", menu_items);
                                      menuCategories.put(category_number, menuCategory);
                                      category_number++;         
                                      
                                }
                          

                    
                }
                
                return menuCategories;
                
                
            } catch (SQLException | ClassNotFoundException | MachineDataException ex) {
                Logger.getLogger(MenuManager.class.getName()).log(Level.SEVERE, null, ex);
                
                return null;
            }
        
        
    }

    
}
