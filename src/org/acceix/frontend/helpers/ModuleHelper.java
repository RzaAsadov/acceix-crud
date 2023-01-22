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

package org.acceix.frontend.helpers;

import org.acceix.frontend.templates.NTemplateLoader;
import de.neuland.pug4j.Pug4J;
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.exceptions.PugCompilerException;
import de.neuland.pug4j.template.FileTemplateLoader;
import de.neuland.pug4j.template.PugTemplate;
import org.acceix.frontend.database.AdminFunctions;
import org.acceix.frontend.models.RoleModel;
import org.acceix.frontend.web.commons.FrontendSecurity;
import org.acceix.frontend.web.commons.DataUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level; 
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.acceix.ndatabaseclient.exceptions.MachineDataException;
import org.acceix.logger.NLog;
import org.acceix.logger.NLogBlock;
import org.acceix.logger.NLogger;

/**
 *
 * @author zrid
 */
public abstract class ModuleHelper {
    

        private Map<String,Object> globalEnvs;
        private final DataUtils nDataTools = new DataUtils();

        private HttpServletResponse m_ServletResponse;
        private HttpServletRequest m_ServletRequest;
        private HttpSession httpSess;
        private RequestObject requestObject;
        
        
        private Map<String, Object> publicDataModel = new LinkedHashMap<>();
        
        private Map<String, Object> privateDataModel = new LinkedHashMap<>();
        
        private Map<String, String> formDataMap;
        
        ModuleSettings moduleSettings = new ModuleSettings();
        
        private AdminFunctions databaseAdminFunctions;

        
        private int userId = -1;
        private int groupId = -1;
        private int rolesetId = -1;
        private String username = "";
        private String token = "";
        List<String> rolesOfUser;
        private String domain = "";
        private String defaultPage = "";
        private String mainPage = "";
        private boolean authenticatedByToken = false;
        
        private String moduleNicename = "";
        
        
        
        private boolean testmode=false;
        

        public void init() {
            setDatabaseAdminFunctions(new AdminFunctions(getGlobalEnvs(),getUsername()));
        }

        

        public AdminFunctions getDatabaseAdminFunctions() {
            return databaseAdminFunctions;
        }

        public void setDatabaseAdminFunctions(AdminFunctions databaseAdminFunctions) {
            this.databaseAdminFunctions = databaseAdminFunctions;
        }

        
        public void setGobalEnv (Map<String,Object> envs) {
                this.globalEnvs = envs;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public void setRolesetId(int rolesetId) {
            this.rolesetId = rolesetId;
        }



        public boolean isAuthReuqiredFor(String action) {
                        return getAction(action).isRequireAuth();
        }
        
        public boolean isRoleAviableForUser(String rolename) {
            List<String> rolesList = getRolesOfUser();
            if (rolesList==null) {
                return false;
            } else  {
                return rolesList.contains(rolename);
            }   
        }
        
        
        public String getRequestedAction() {
            return getRequestObject().getAction();
        }
        

        public void setupWebModule(HttpServletResponse response,HttpServletRequest request) {
                this.m_ServletRequest = request;
                this.m_ServletResponse = response;            
        }        

        
        public final void processModule () throws ClassNotFoundException, SQLException, MachineDataException {
            
                initNewDataModel();
            
                addToDataModel("api_url",getGlobalEnvs().get("api_url"));
                
                //System.out.println("processModule called #1 , action=" + getRequestedAction() + ", module=" + getModuleName());

                if (getAction(getRequestedAction())==null) {
                    return;
                }
                
                //System.out.println("processModule called #2");
                
                        setSession();
                        authbytoken();
                    
                        if (isUserAuthenticatedBySession()) {
                            
                                setUserId((int)getSessionAttribute("userid")); // get from session
                                setToken((String)getSessionAttribute("token")); // get from session
                                setupUserEnvironments();
                                
                        } else if (isAuthenticatedByToken()) {

                                setupUserEnvironments();
                            
                        } else {
                            
                             if (isAuthReuqiredFor(getRequestedAction())) {
                                 addToDataModel("result","error");
                                 addToDataModel("message","Auth required");
                                 renderData();
                                 return;
                             }

                        }
                        
                       
                //System.out.println("processModule called #3");

                
                if (getRequestedAction()!=null) {
                    
                        ActionSettings actionSettings = getAction(getRequestedAction());
                        if (actionSettings != null) {
                            actionSettings.runAction();
                        } else {
                            FrontendSecurity.securityCaseHandler(getHttpServletRequest(), getHttpServletResponse());
                        }
                        
                } else {
                        FrontendSecurity.securityCaseHandler(getHttpServletRequest(), getHttpServletResponse());
                }
                
            
        }
        
        public void setupUserEnvironments() throws MachineDataException, ClassNotFoundException, SQLException {
            

                if (getUserId()==0) {
                    
                        List<String> roles = new LinkedList<>();
                        
                        
                        getDatabaseAdminFunctions().getAllRoleList().forEach( (RoleModel rolemodel) -> {
                            roles.add(rolemodel.getRolename());
                        });
                        

                        roles.add("netondoadmin");
                    
                        setRolesOfUser(roles);
                        setGroupId(0);
                        setRolesetId(0);
                        setUsername((String)getGlobalEnvs().get("admin_user"));
                        setDomain((String)getGlobalEnvs().get("admin_domain"));
                        

                        
                } else {
            
                        setRolesOfUser(getDatabaseAdminFunctions().getUserRoles(getUserId()));
                        setGroupId(getDatabaseAdminFunctions().getGroupIdOfUser(getUserId()));
                        setRolesetId(getDatabaseAdminFunctions().getRolesetIdOfUser(getUserId()));
                        setUsername(getDatabaseAdminFunctions().getUsernameOfUser(getUserId()));

                        
                        setDomain(getDatabaseAdminFunctions().getDomainById(
                                                    getDatabaseAdminFunctions()
                                                    .getDomainIdOfUser(getUserId())));
                        
                        addToDataModel("title", getDatabaseAdminFunctions().getTitleOfDomain(getDomain()));
                        //setDefaultPage(getDatabaseAdminFunctions().getDefaultPageOfUser(getUserId()));

                }                        
                

                 addToDataModel("username", getUsername());

                
        }
        
        public String getRequestedDomain() {
            String requestedHostname = getHttpServletRequest().getServerName();
            return requestedHostname;
        }

        
        public abstract void construct();
        
        public abstract ModuleHelper getInstance();

        public void setRequestObject(RequestObject requestObject) {
            this.requestObject = requestObject;
        }

        
        

        public Map<String, Object> getGlobalEnvs() {
            return globalEnvs;
        }
        
        public String getRequestIP() {
            if (getGlobalEnvs().get("reverse_proxy_use").toString().equals("yes")) {
            
                String ip = m_ServletRequest.getHeader(getGlobalEnvs().get("reverse_proxy_ip_header").toString());
                
                if (ip.contains(".")) {
                    ip = ip.split(",")[0];
                }
                 
                return ip;
            } else {
                return m_ServletRequest.getRemoteAddr();  
            }
        }

        public String getRequestHeader(String name) {
            
           
            return  m_ServletRequest.getHeader(name);
            
            
        }
        

        
        public PrintWriter getOutputWriter() {
            try {
                return m_ServletResponse.getWriter();
            } catch (IOException ex) {
                Logger.getLogger(ModuleHelper.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        
        public void sendToClient(String content) {
            getOutputWriter().write(content);
        }
        
        
        public void addToDataModel (String key,Object value) {
            publicDataModel.put(key, value);
        }
        
        public void removeFromDataModel(String key) {
            publicDataModel.remove(key);
        }
        
        public void initNewDataModel() {
            publicDataModel = new LinkedHashMap<>();
        }

 
        
        public void addToPrivateDataModel (String key,Object value) {
            privateDataModel.put(key, value);
        }
        
        public void getFromPrivateDataModel(String key) {
            privateDataModel.get(key);
        }


        public void getFromPrivateDataModelOrDefault(String key) {
            privateDataModel.get(key);
        }
        
        public boolean isUserAuthenticatedBySession () {
            
            httpSess = getHttpServletRequest().getSession(false);

            if (httpSess != null) {
                if (getSessionAttribute("authenticated")!=null) {
                    return (boolean)getSessionAttribute("authenticated");
                } else {
                    return false;
                }                
            } else {
                return false;
            }              
            

        }
        
        public void createSession() {
            httpSess = getHttpServletRequest().getSession(true);            
        }
        
        public void setSession() {
            httpSess = getHttpServletRequest().getSession(true);
        }

        public void authbytoken () {
            
                        String mytoken = (String) getParameter("token");
            
                        if (mytoken == null) return;
                  
                        String useridOfTokenInBase64 = mytoken.split(":")[0];
                        String passwordOfTokenInBase64 = mytoken.split(":")[1];
                        
                        
                        int user_id = Integer.parseInt(new String(Base64.getDecoder().decode(useridOfTokenInBase64)));
                        

                                String passwordFromClientInMd5 = new String(Base64.getDecoder().decode(passwordOfTokenInBase64));

                                try {
                                    String passwordFromDatabase = databaseAdminFunctions.getPasswordOfUser(user_id);
                                    if (passwordFromDatabase == null) { 
                                        throw new NullPointerException("passwordFromDatabase is null");
                                    }


                                            MessageDigest m = MessageDigest.getInstance("MD5");
                                            m.update(passwordFromDatabase.getBytes(),0,passwordFromDatabase.length());

                                            String passwordFromDatabaseInMd5 = new BigInteger(1,m.digest()).toString(16); 

                                            if (passwordFromClientInMd5.equals(passwordFromDatabaseInMd5)) {
                                                setAuthenticatedByToken(true);
                                                setUserId(user_id);
                                                setToken(mytoken);
                                                
                                            } else {
                                                setAuthenticatedByToken(false);
                                            }
                                            
                                } catch (NoSuchAlgorithmException | MachineDataException | ClassNotFoundException | SQLException ex) {
                                    Logger.getLogger(ModuleHelper.class.getName()).log(Level.SEVERE, null, ex);
                                    setAuthenticatedByToken(false);
                                }
            
        }
        


        
        

        public Map<String, String> getRequestFormDataInputMap() {
            return formDataMap;
        }

        

        public Map<String, Object> getDataModel() {
            return publicDataModel;
        }
        
        public void addAction(ActionSettings actionSettings) {
            moduleSettings.addActionToModule(actionSettings.getName(),actionSettings);
        } 
        
        public ActionSettings getAction(String action) {
            if (moduleSettings.getAction(action)!= null) {
                return moduleSettings.getAction(action);
            } else {
                    return moduleSettings.getAction("function");
            }
        }
         
        public String getModuleName() {
            return moduleSettings.getName();
        }
        
        public void setModuleName(String moduleName) {
            moduleSettings.setName(moduleName);
        }

        
        
        public String getModuleNiceName() {
            return moduleSettings.getNiceName();
        }
        
        public void setModuleNiceName(String moduleName) {
            moduleSettings.setNiceName(moduleName);
        }        

        public ModuleSettings getModuleSettings() {
            return moduleSettings;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }


        public void setRolesOfUser(List<String> rolesOfUser) {
            this.rolesOfUser = rolesOfUser;
        }

        public List<String> getRolesOfUser() {
            return rolesOfUser;
        }

        

        public void setAuthenticatedByToken(boolean authenticated) {
            this.authenticatedByToken = authenticated;
        }

        public boolean isAuthenticatedByToken() {
            return authenticatedByToken;
        }



        public String getMainPage() {
            return mainPage;
        }

        public void setMainPage(String mainPage) {
            this.mainPage = mainPage;
        }
        

        public int getGroupId() {
            return groupId;
        }

        public int getRolesetId() {
            return rolesetId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }



        public String getDefaultPage() {
            return defaultPage;
        }

        public void setDefaultPage(String defaultPage) {
            this.defaultPage = defaultPage;
        }

        public HttpServletResponse getHttpServletResponse() {
            return m_ServletResponse;
        }

        public HttpServletRequest getHttpServletRequest() {
            return m_ServletRequest;
        }

        public RequestObject getRequestObject() {
            return requestObject;
        }

        public Object getParameter(String key) {
            return getRequestObject().getParams().get(key);
        }
        
        public String getParameterOrDefault(String key,String default_val) {
            
            Object param = getRequestObject().getParams().get(key);
            
            if (param == null) {
                return default_val;
            } else {
                return (String)param;
            }
            
        }
        

        public void setContentType(String contentType) {
            getHttpServletResponse().setContentType(contentType);
        }
        
        public void setCharacterEncoding(String encodingType) {
            getHttpServletResponse().setCharacterEncoding(encodingType);
        }
        
        public void setSessionAttribute(String key,Object value) {
            this.httpSess.setAttribute(key, value);
        }
        
        public Object getSessionAttribute(String key) {
            
            Object attribute = httpSess.getAttribute(key);
            if (attribute==null) {
                return null;
            } else {
                return attribute;
            }
        }
        
        public void renderData () {
            try {
                renderData(null);
            } catch (IOException ex) {
                Logger.getLogger(ModuleHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public void renderDataDebug () throws IOException {
            System.out.println(nDataTools.beautyfyJson(nDataTools.mapToJsonString(getDataModel())));
        }
        

        public void renderData (String viewFilename) throws IOException {
            
            if (viewFilename==null) {
                //setContentType("application/json; charset = UTF-8");
                setContentType("application/json");
                setCharacterEncoding("utf-8");
                sendToClient(nDataTools.beautyfyJson(nDataTools.mapToJsonString(getDataModel())));
            } else {
                
                if (getParameter("of")!=null ) {
                                          
                    
                    
                    
                    switch ((String)getParameter("of")) {
                        case "json":
                            setContentType("application/json");
                            setCharacterEncoding("utf-8");
                            sendToClient(nDataTools.mapToJsonString(getDataModel()));
                            break;
                        case "json2":
                            setContentType("application/json"); 
                            setCharacterEncoding("utf-8");
                            sendToClient(nDataTools.beautyfyJson(nDataTools.mapToJsonString(getDataModel())));                            

                            break;
                        default:
                            throw new UnsupportedOperationException("Only JADE and JSON format supported !!!");
                    }
                } else {
                    
                        if (getUserId()==0 && getGlobalEnvs().get("admin_domain")!=null) {
                            setDomain((String)getGlobalEnvs().get("admin_domain"));
                            addToDataModel("title", getDomain());
                        } else {
                            addToDataModel("title", "Admin panel - domain not set for superadmin !");
                        }
                    
                    
                    Map<String,Boolean> rolesMap = new LinkedHashMap<>();
                    
                    List rolesList = getRolesOfUser();
                    
                    if (rolesList != null) {

                        getRolesOfUser().forEach((k) -> {
                            rolesMap.put(k, Boolean.TRUE);
                        });
                        
                    }
                        
                    
                    addToDataModel("roles",rolesMap);
                    
                    
                    try {

                        setContentType("text/html;charset=UTF-8");

                        
                        if (viewFilename.startsWith("/views/")) {
                            viewFilename = viewFilename.substring(6, viewFilename.length());
                            if (new File(getGlobalEnvs().get("custom_views_path") + viewFilename + ".pug").exists()) {
                                sendToClient(Pug4J.render(getGlobalEnvs().get("custom_views_path") + viewFilename+ ".pug" , getDataModel(),true));
                            } else {
                                NLogger.logger(NLogBlock.WEB,NLog.ERROR,getModuleName(),getModuleName(),getUsername(),"Template not found, path=" + getGlobalEnvs().get("views_path") + viewFilename);
                            }
                        } else if (viewFilename.startsWith("/embed/")) {
                            
                            viewFilename = viewFilename.substring(6, viewFilename.length());
                            
                            var config = new PugConfiguration();
                            var loader = new NTemplateLoader();


                            config.setTemplateLoader(loader);
                            
                            PugTemplate template = config.getTemplate(viewFilename + ".pug" );

                            sendToClient(Pug4J.render(template, getDataModel(),true));                            
                        } else {
                            if (new File(getGlobalEnvs().get("admin_views_path") + viewFilename + ".pug").exists()) {
                                
                                PugConfiguration config = new PugConfiguration();
                                FileTemplateLoader loader = new FileTemplateLoader((String)getGlobalEnvs().get("admin_views_path"));
                                config.setTemplateLoader(loader);
                                PugTemplate template = config.getTemplate(viewFilename);


                                sendToClient(Pug4J.render(template , getDataModel(),true));
                                //sendToClient(Pug4J.render(getGlobalEnvs().get("admin_views_path") + viewFilename + ".pug" , getDataModel(),true));
                            } else {
                                NLogger.logger(NLogBlock.WEB,NLog.ERROR,getModuleName(),"modulehelper",getUsername(), "Template not found, path=" + getGlobalEnvs().get("admin_views_path") + viewFilename);
                            }                            
                        }
                        
                        //sendToClient("Hello");
                    } catch (IOException | PugCompilerException ex) {
                        Logger.getLogger(ModuleHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }

            }
        }
        
    
}
