package avl.sv.client;

import static avl.sv.client.AdvancedVirtualMicroscope.setStatusText;
import avl.sv.server.LoginPort;
import avl.sv.server.LoginPort_Service;
import avl.sv.shared.AVM_Properties;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.KVStoreRef;
import avl.sv.shared.SessionManagerKVStore;
import avl.sv.shared.User_KVStore;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.ws.WebServiceException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;

public class LoginDialog extends javax.swing.JDialog {
    KVStore kvstore;
       
    public LoginDialog(Frame frame) {
        super(frame, true);
        initComponents();
        populateServerComboBox();
        jPasswordFieldPassword0.setText("");
        jCheckBoxAutoLogin.setSelected(isAutoLoginEnabled());
        
        String databaseURL = AVM_Properties.getProperty(AVM_Properties.DATABASE_HOSTS_KEY);
        if (databaseURL != null) {
            jTextFieldDatabaseHosts.setText(databaseURL);
        } else {
            jTextFieldDatabaseHosts.setText("");
        }
        String databaseName = AVM_Properties.getProperty(AVM_Properties.DATABASE_NAME_KEY);
        if (databaseName != null) {
            jTextFieldDatabaseName.setText(databaseName);
        } else {
            jTextFieldDatabaseName.setText("");
        }
        String username = AVM_Properties.getProperty(AVM_Properties.USERNAME_KEY);
        if (username != null) {
            jTextFieldUsername.setText(username);
        } else {
            jTextFieldUsername.setText("");
        }
        EventQueue.invokeLater(() -> {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                }
            });
            populateServerComboBox();
            getRootPane().setDefaultButton(jButtonLoginServer);
        });
    }

    public boolean isAutoLoginEnabled(){
        String autoDestination = AVM_Properties.getProperty(AVM_Properties.AUTO_LOGIN_DESTINATION_KEY);
        return ((autoDestination != null) && (!autoDestination.isEmpty()));
    }
    
    protected String getUsername(){
        return jTextFieldUsername.getText();
    }
    
    protected String getPassword(){
        return new String(jPasswordFieldPassword0.getPassword());
    }
    
    String getServerURL() {
        String url = (String) jComboBoxServerURL.getSelectedItem();
        for (int i = 0; i < jComboBoxServerURL.getItemCount(); i++){
            if (jComboBoxServerURL.getItemAt(i).equals(url)){
                return url;
            }
        }
        addServerURL(url);
        return url;
    }

    private void populateServerComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        ArrayList<String> serverURLs = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            String url = AVM_Properties.getProperty("Server_" + String.valueOf(i) + "_URL");
            if (url != null) {
                url = url.replace("http://", "");
                url = url.replace("https://", "");
                serverURLs.add(url);
            } else {
                break;
            }
        }
        String host = "localhost";
        try {
            final BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
            URL codebase = bs.getCodeBase();
            if (codebase != null) {
                host = codebase.getHost();
            }
        } catch (Exception ex) {
            System.out.println("Failed to find codebase");
        }

        if (serverURLs.isEmpty()){
            serverURLs.add(host);
        } else {
            if (!serverURLs.get(0).equals(host)){
                serverURLs.add(0, host);
            }
        }
        
        for (String url:serverURLs){
            model.addElement(url);
        }        
        jComboBoxServerURL.setModel(model);
    }
    
    private void addServerURL(String str){
        str = str.replace("http://", "");
        int idx = jComboBoxServerURL.getItemCount();
        AVM_Properties.setProperty("Server_"+String.valueOf(idx)+"_URL", str);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPaneLoginType = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jComboBoxServerURL = new javax.swing.JComboBox<String>();
        jButtonLoginServer = new javax.swing.JButton();
        jButtonRegisterServer = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTextFieldDatabaseHosts = new javax.swing.JTextField();
        jTextFieldDatabaseName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButtonRegisterKVStore = new javax.swing.JButton();
        jButtonLoginKVStore = new javax.swing.JButton();
        jTextFieldUsername = new javax.swing.JTextField();
        jPasswordFieldPassword0 = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxAutoLogin = new javax.swing.JCheckBox();

        setTitle("Login Form");
        setLocationByPlatform(true);
        setModal(true);
        setResizable(false);

        jTabbedPaneLoginType.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPaneLoginTypeStateChanged(evt);
            }
        });

        jComboBoxServerURL.setEditable(true);
        jComboBoxServerURL.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxServerURLItemStateChanged(evt);
            }
        });

        jButtonLoginServer.setText("Login");
        jButtonLoginServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoginServerActionPerformed(evt);
            }
        });

        jButtonRegisterServer.setText("Register");
        jButtonRegisterServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRegisterServerActionPerformed(evt);
            }
        });

        jLabel3.setText("URL");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonLoginServer, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRegisterServer, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 19, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jComboBoxServerURL, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxServerURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonLoginServer)
                    .addComponent(jButtonRegisterServer))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jTabbedPaneLoginType.addTab("Server", jPanel1);

        jTextFieldDatabaseHosts.setText("localhost:5000");
        jTextFieldDatabaseHosts.setToolTipText("Comma separated");
        jTextFieldDatabaseHosts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDatabaseHostsActionPerformed(evt);
            }
        });

        jTextFieldDatabaseName.setText("kvStore");

        jLabel6.setText("Store Name");

        jLabel4.setText("Hosts");

        jButtonRegisterKVStore.setText("Register");
        jButtonRegisterKVStore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRegisterKVStoreActionPerformed(evt);
            }
        });

        jButtonLoginKVStore.setText("Login");
        jButtonLoginKVStore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoginKVStoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldDatabaseName)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonLoginKVStore, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRegisterKVStore, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jTextFieldDatabaseHosts))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldDatabaseHosts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldDatabaseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonRegisterKVStore)
                    .addComponent(jButtonLoginKVStore))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneLoginType.addTab("KVStore", jPanel2);

        jTextFieldUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldUsernameActionPerformed(evt);
            }
        });

        jLabel1.setText("Username");

        jLabel2.setText("Password");

        jCheckBoxAutoLogin.setText("Remember me");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneLoginType)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextFieldUsername)
                            .addComponent(jPasswordFieldPassword0))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxAutoLogin)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jPasswordFieldPassword0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxAutoLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPaneLoginType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonRegisterServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRegisterServerActionPerformed
        String str = getServerURL();
        try {
            URL url = Plugins_Server.getloginPortLocation(str);
            RegisterDialog diag = new RegisterDialog(null) {
                @Override
                public void register(String username, String password) {
                    LoginPort loginPort;
                    try {
                        loginPort = new LoginPort_Service(url).getLoginPortPort();
                    } catch (WebServiceException ex) {
                        String msg = "Failed to connect to the server at " + url;
                        AdvancedVirtualMicroscope.setStatusText(msg, 5000);
                        JOptionPane.showMessageDialog(null, msg);
                        return;
                    }
                    if (loginPort == null) {
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cannot connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String result = loginPort.registerUser(username, password);
                    if (result.startsWith("error:")) {
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), result, "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        isCanceled = false;
                        setVisible(false);
                        JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "User account created");
                        AdvancedVirtualMicroscope.setStatusText("User account created", 5 * 1000);
                    }
                }
            };
            diag.setVisible(true);
            if (!diag.isCanceled()){
                jTextFieldUsername.setText(diag.getUsername());
                jPasswordFieldPassword0.setText(new String(diag.getPassword()));
            }
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(null, "Could not connect to server at " + str);
        }
    }//GEN-LAST:event_jButtonRegisterServerActionPerformed

    private void jButtonLoginServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoginServerActionPerformed
        if (0 == loginToServer()) {
            if (jCheckBoxAutoLogin.isSelected()) {
                setAutoLogin("server");
            } else {
                autologinClear();
            }
            setVisible(false);
        }
    }//GEN-LAST:event_jButtonLoginServerActionPerformed

    private int loginToServer(){
        String url = getServerURL();;
        String username = getUsername();
        String password = getPassword();
        try {
            AVM_Plugin[] plugins = Plugins_Server.login(username, password, url);
            if (plugins == null){
                return -1;
            }
            AdvancedVirtualMicroscope.addPlugins(plugins);
            AdvancedVirtualMicroscope.setStatusText("Logged into server", 4 * 1000);
            AdvancedVirtualMicroscope.setUsername(username);
        } catch (WebServiceException | MalformedURLException ex) {
            setStatusText("Failed to connect server at: " + url, 0);
            Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        AVM_Properties.setProperty(AVM_Properties.USERNAME_KEY, jTextFieldUsername.getText());
        return 0;
    }
    
    private void jTextFieldDatabaseHostsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDatabaseHostsActionPerformed

    }//GEN-LAST:event_jTextFieldDatabaseHostsActionPerformed

    private void jComboBoxServerURLItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxServerURLItemStateChanged

    }//GEN-LAST:event_jComboBoxServerURLItemStateChanged

    private void jButtonRegisterKVStoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRegisterKVStoreActionPerformed
        
        RegisterDialog diag = new RegisterDialog(null) {
            @Override
            public void register(String username, String password) {
                String[] hhosts = {jTextFieldDatabaseHosts.getText()};
                String name = jTextFieldDatabaseName.getText();
                KVStoreConfig kconfig = new KVStoreConfig(name, hhosts);
                try {
                   kvstore = KVStoreFactory.getStore(kconfig);
                } catch (WebServiceException ex) {
                    String msg = "Failed to connect to the database";
                    AdvancedVirtualMicroscope.setStatusText(msg, 5000);
                    JOptionPane.showMessageDialog(null, msg);
                    return;
                }      
                String result = User_KVStore.registerUser(getUsername(), new String(getPassword()));
                if (result.startsWith("error:")) {
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), result, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    isCanceled = false;
                    setVisible(false);
                    JOptionPane.showMessageDialog((Frame) SwingUtilities.getWindowAncestor(this), "User account created");
                    AdvancedVirtualMicroscope.setStatusText("User account created", 5 * 1000);
                }
            }
        };
        diag.setVisible(true);
        if (!diag.isCanceled()){
            jTextFieldUsername.setText(diag.getUsername());
            jPasswordFieldPassword0.setText(new String(diag.getPassword()));
        }
    }//GEN-LAST:event_jButtonRegisterKVStoreActionPerformed

    private void jTextFieldUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldUsernameActionPerformed
    }//GEN-LAST:event_jTextFieldUsernameActionPerformed

    private void jButtonLoginKVStoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoginKVStoreActionPerformed
        if (0 == loginToDatabase()) {
            if (jCheckBoxAutoLogin.isSelected()){
                setAutoLogin("database");
            } else {
                autologinClear();
            }
            setVisible(false);
        }
    }//GEN-LAST:event_jButtonLoginKVStoreActionPerformed

    private void jTabbedPaneLoginTypeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPaneLoginTypeStateChanged
            switch (jTabbedPaneLoginType.getSelectedIndex()){
                case 0:
                    getRootPane().setDefaultButton(jButtonLoginServer);
                    break;
                case 1:
                    getRootPane().setDefaultButton(jButtonLoginKVStore);
                    break;
            }
    }//GEN-LAST:event_jTabbedPaneLoginTypeStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLoginKVStore;
    private javax.swing.JButton jButtonLoginServer;
    private javax.swing.JButton jButtonRegisterKVStore;
    private javax.swing.JButton jButtonRegisterServer;
    private javax.swing.JCheckBox jCheckBoxAutoLogin;
    private javax.swing.JComboBox<String> jComboBoxServerURL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField jPasswordFieldPassword0;
    private javax.swing.JTabbedPane jTabbedPaneLoginType;
    private javax.swing.JTextField jTextFieldDatabaseHosts;
    private javax.swing.JTextField jTextFieldDatabaseName;
    private javax.swing.JTextField jTextFieldUsername;
    // End of variables declaration//GEN-END:variables

    public int autologin() {
        if (!isAutoLoginEnabled()){
            return -2;
        }
        String autologinPassword = AVM_Properties.getProperty(AVM_Properties.AUTO_LOGIN_PASSWORD_KEY);
        jPasswordFieldPassword0.setText(autologinPassword);
        int result = -1;
        switch (AVM_Properties.getProperty(AVM_Properties.AUTO_LOGIN_DESTINATION_KEY)){
            case "server":
                result = loginToServer();
                break;
            case "database":
                result = loginToDatabase();
                break;
            default:
                JOptionPane.showMessageDialog(null, "Auto login information was found invalid and cleared");
                autologinClear();
                break;
        }
        if (result!=0){
            autologinClear();
        }
        return result;
    }
    
    public void setAutoLogin(String destination){
        AVM_Properties.setProperty(AVM_Properties.AUTO_LOGIN_DESTINATION_KEY, destination);
        AVM_Properties.setProperty(AVM_Properties.AUTO_LOGIN_PASSWORD_KEY, getPassword());
    }

    private int loginToDatabase() {
        String hosts = jTextFieldDatabaseHosts.getText().trim();
        String name = jTextFieldDatabaseName.getText();
        String username = getUsername();
        String password = getPassword();

        AVM_Properties.setProperty(AVM_Properties.DATABASE_HOSTS_KEY, hosts);
        AVM_Properties.setProperty(AVM_Properties.DATABASE_NAME_KEY, name);
        
        if (null == KVStoreRef.getRef()){
            String str = "Failed to connect to the database";
            setStatusText(str, 5000);
            JOptionPane.showMessageDialog(this, str);
            return -1;
        }          
        SessionManagerKVStore sessionManager = new SessionManagerKVStore();
        String sessionID = sessionManager.login(username, password);
        if (sessionID.contains("error:")){
            JOptionPane.showMessageDialog(null, "Failed to login to server with " + sessionID);
            return -3;
        }
        AVM_Session session = new AVM_Session(username, sessionID);
        AVM_Plugin[] plugins = Plugins_KVStore.login(session);
        if (plugins == null) {
            return -2;
        }
        AdvancedVirtualMicroscope.addPlugins(plugins);
        AdvancedVirtualMicroscope.setStatusText("Logged into database", 4 * 1000);
        AdvancedVirtualMicroscope.setUsername(username);

        AVM_Properties.setProperty(AVM_Properties.USERNAME_KEY, jTextFieldUsername.getText());
        return 0;
    }

    public void autologinClear() {
        AVM_Properties.setProperty(AVM_Properties.AUTO_LOGIN_PASSWORD_KEY, "");
        AVM_Properties.setProperty(AVM_Properties.AUTO_LOGIN_DESTINATION_KEY, "");
    }
    
}
