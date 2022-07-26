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

package org.acceix.frontend.helpers;
import org.acceix.ndatabaseclient.DataTypes;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.ndatabaseclient.DataConnector;

public final class DbMetaData {
    
        private final DataConnector nDataConnector;
        private int dbtype;
        
        private final Map<String,Object> tablesOfDatabase = new LinkedHashMap<>();


        public DbMetaData(DataConnector nDataConnector) {
            this.nDataConnector = nDataConnector;
            setDbtype(nDataConnector.getDbtype());
        }
         
        public int getDbtype() {
            return dbtype;
        }

        public void setDbtype(int dbtype) {
            this.dbtype = dbtype;
        }    
        
        public DataConnector getnDataConnector() {
            return nDataConnector;
        }        
    
        public List<String> getTableList() {
            
                List<String> tableList = new LinkedList<>();

                DatabaseMetaData md;
                try {


                        md = getnDataConnector().getConnection().getMetaData();

                        ResultSet rs = md.getTables(null, null, "%", null);
                        while (rs.next()) {
                          tableList.add(rs.getString(3));
                        }                  

                } catch (ClassNotFoundException | SQLException ex) {
                    Logger.getLogger(DbMetaData.class.getName()).log(Level.SEVERE, null, ex);
                }


                return tableList;
            
        }
        
        public Map<String,Object> getColumn(String tableName,String columName) {
            return getColumnList(tableName).get(columName.toLowerCase());
        }
        
        public Map<String,Map<String,Object>> getColumnList(String table) {
            
                if (tablesOfDatabase.get(table) != null) {
                    return (Map<String,Map<String,Object>> )tablesOfDatabase.get(table);
                } else {
                    return getColumnListFromDb(table);
                }
            
        }
        
        private Map<String,Map<String,Object>> getColumnListFromDb(String table) {
            
                Map<String,Map<String,Object>> columnsOfTable = new LinkedHashMap<>();

                
                try {
                    
                    DataTypes dataTypes = new DataTypes();

                    ResultSet rs = getnDataConnector().getConnection()
                                                      .getMetaData()
                                                      .getColumns(null, null, table, "%");
                     
                    while (rs.next()) {
                        
                        String columnName = rs.getString(4);
                        String columnType = rs.getString(6);
                        String columnSize = rs.getString(7);
                        String columnIsNullable = rs.getString(18);
                        
                        /*if (table != null) {
                            System.out.println("Table:" + table + " , column: " + columnName + ", dt:" + columnType.toLowerCase() + ", dt:" + rs.getString(5) + ", dd:" + dataTypes.stringToDataType(columnType.toLowerCase()));
                        }*/
                        
                        Map<String,Object> column = new LinkedHashMap<>();

                        column.put("dataType",dataTypes.stringToDataType(columnType.toLowerCase()));
                       

                        if (columnType.toLowerCase().equals("varchar") || columnType.toLowerCase().equals("char")) 
                              column.put("length",Integer.valueOf(columnSize));

                        columnsOfTable.put(columnName.toLowerCase(), column);
                        
                    }                  

                } catch (ClassNotFoundException | SQLException ex) {
                    Logger.getLogger(DbMetaData.class.getName()).log(Level.SEVERE, null, ex);
                }

                tablesOfDatabase.put(table, columnsOfTable);
                
                return columnsOfTable;

        }        
    
}
