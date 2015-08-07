package avl.sv.shared.model.featureGenerator.jocl;

import avl.sv.shared.AVM_Properties;
import java.awt.Component;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.cl_platform_id;

public final class JOCL_Configure extends JFrame {
    boolean enableSaving = false;
        
    public JOCL_Configure() {
        initComponents();
            
        ArrayList<Platform> platforms = getPlatforms();
        DefaultComboBoxModel<Platform> model = new DefaultComboBoxModel<>();
        for (Platform platform:platforms){
            model.addElement(platform);
        }
        jComboBoxPlatform.setModel(model);
        if (platforms.isEmpty()){
            for (Component c : getContentPane().getComponents()) {
                c.setVisible(false);
            }
            JTextArea noDevicesFound = new JTextArea("No OpenCL devices found");
            noDevicesFound.setSize(getSize());
            add(noDevicesFound);
            noDevicesFound.setVisible(true);
            return;
        }       

        int platformIdx = getSelectedPlatformIndex();
        int deviceIdx = getSelectedDeviceIndex();
        
        if ((platformIdx == -1) || (deviceIdx == -1)){
            enableSaving = true;
            if ((platformIdx == -1)){
                platformIdx = 0;
            }
            if ((deviceIdx == -1)){
                deviceIdx = 0;
            }
        }

        jComboBoxPlatform.setSelectedIndex(platformIdx);
        jComboBoxDevice.setSelectedIndex(deviceIdx);
        enableSaving = true;
        
    }
    
    public static int getSelectedPlatformIndex(){
        String platformIdx = AVM_Properties.getProperty(AVM_Properties.Name.CL_platform_Index);
        if (platformIdx != null) {
            return Integer.valueOf(platformIdx);
        }      
        return 0;
    }
    
    public static int getSelectedDeviceIndex(){
        String deviceIdx = AVM_Properties.getProperty(AVM_Properties.Name.CL_device_Index);
        if (deviceIdx != null) {
            return Integer.valueOf(deviceIdx);
        }
        return 0;
    }

    public static ArrayList<Platform> getPlatforms() {
        int num_entries = 10;
        cl_platform_id platforms_Ids[] = new cl_platform_id[num_entries];
        int num_platforms[] = new int[1];
        CL.clGetPlatformIDs(num_entries, platforms_Ids, num_platforms);
        ArrayList<Platform> platforms = new ArrayList<>();
        for (int i = 0; i < num_platforms[0]; i++) {
            Platform platform = new Platform(platforms_Ids[i]);
            platforms.add(platform);
        }
        return platforms;
    }
    private String getString(Device device, int paramID){
        int param_value_size = 200;
        byte buffer[] = new byte[param_value_size];
        long param_value_size_ret[] = new long[1];
        CL.clGetDeviceInfo(device.deviceID, paramID, param_value_size, Pointer.to(buffer), param_value_size_ret);
        return new String(buffer);
    }
    
    private long getLongParam(Device device, int paramID){
        int param_value_size = 8;
        byte buffer[] = new byte[param_value_size];
        long param_value_size_ret[] = new long[1];
        CL.clGetDeviceInfo(device.deviceID, paramID, param_value_size, Pointer.to(buffer), param_value_size_ret);
        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }
    
    private int getIntParam(Device device, int paramID){
        int param_value_size = 8;
        byte buffer[] = new byte[param_value_size];
        long param_value_size_ret[] = new long[1];
        CL.clGetDeviceInfo(device.deviceID, paramID, param_value_size, Pointer.to(buffer), param_value_size_ret);
        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
    
    private long getMemoryTotal(Device device){
        int param_value_size = 8;
        byte buffer[] = new byte[param_value_size];
        long param_value_size_ret[] = new long[1];
        CL.clGetDeviceInfo(device.deviceID, CL.CL_DEVICE_GLOBAL_MEM_SIZE, param_value_size, Pointer.to(buffer), param_value_size_ret);
        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }
    
    public static long getMemoryTotal(){
        Device device = getPlatforms().get(getSelectedPlatformIndex()).devices.get(getSelectedDeviceIndex());       
        int param_value_size = 8;
        byte buffer[] = new byte[param_value_size];
        long param_value_size_ret[] = new long[1];
        CL.clGetDeviceInfo(device.deviceID, CL.CL_DEVICE_GLOBAL_MEM_SIZE, param_value_size, Pointer.to(buffer), param_value_size_ret);
        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }
        
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JOCL_Configure.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JOCL_Configure().setVisible(true);
            }
        });
    }    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxPlatform = new javax.swing.JComboBox<Platform>();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxDevice = new javax.swing.JComboBox<Device>();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Configure JOCL");

        jComboBoxPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPlatformActionPerformed(evt);
            }
        });

        jLabel1.setText("Platform");

        jComboBoxDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDeviceActionPerformed(evt);
            }
        });

        jLabel2.setText("Device");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxPlatform, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBoxDevice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPlatformActionPerformed
        Platform platform = (Platform) jComboBoxPlatform.getModel().getSelectedItem();
        if (platform == null){
            return;
        }
        DefaultComboBoxModel<Device> model = new DefaultComboBoxModel<>();
        for (Device device:platform.devices){
            model.addElement(device);
        }
        jComboBoxDevice.setModel(model);
        jComboBoxDevice.setSelectedIndex(0);
        save();
    }//GEN-LAST:event_jComboBoxPlatformActionPerformed

    private void jComboBoxDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDeviceActionPerformed
        Device device = (Device) jComboBoxDevice.getSelectedItem();        
        if (device == null){
            DefaultTableModel model = new DefaultTableModel();
            jTable1.setModel(model);
            return;
        }
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Parameter");
        model.addColumn("Value");
        model.addRow(new Object[]{"CL_DEVICE_VENDOR", getString(device, CL.CL_DEVICE_VENDOR)});
        model.addRow(new Object[]{"CL_DEVICE_VERSION", getString(device, CL.CL_DEVICE_VERSION)});
        model.addRow(new Object[]{"CL_DRIVER_VERSION", getString(device, CL.CL_DRIVER_VERSION)});
        model.addRow(new Object[]{"CL_DEVICE_GLOBAL_MEM_SIZE", String.valueOf(Math.round(getMemoryTotal(device)/Math.pow(2, 20)*100)/100)+" MB"});
        model.addRow(new Object[]{"CL.CL_DEVICE_LOCAL_MEM_SIZE", String.valueOf(getIntParam(device, CL.CL_DEVICE_LOCAL_MEM_SIZE)/Math.pow(10, 3)+ " KB")});
        model.addRow(new Object[]{"CL_DEVICE_MAX_CLOCK_FREQUENCY", String.valueOf(getIntParam(device, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY)+ " MHz")});
        model.addRow(new Object[]{"CL_DEVICE_MAX_COMPUTE_UNITS", String.valueOf(getIntParam(device, CL.CL_DEVICE_MAX_COMPUTE_UNITS))});
        model.addRow(new Object[]{"CL_DEVICE_MAX_MEM_ALLOC_SIZE", String.valueOf(getLongParam(device, CL.CL_DEVICE_MAX_MEM_ALLOC_SIZE)/Math.pow(10, 6)+ " MB")});
        model.addRow(new Object[]{"CL_DEVICE_MAX_PARAMETER_SIZE", String.valueOf(getLongParam(device, CL.CL_DEVICE_MAX_PARAMETER_SIZE)/Math.pow(10, 3)+ " KB")});
        model.addRow(new Object[]{"CL_DEVICE_MAX_WORK_GROUP_SIZE", String.valueOf(getIntParam(device, CL.CL_DEVICE_MAX_WORK_GROUP_SIZE))});
//        model.addRow(new Object[]{"CL_DEVICE_MAX_WORK_GROUP_SIZE", String.valueOf(getIntParam(device, CL.CL_DEVICE_MAX_MEM_ALLOC_SIZE))});

        jTable1.setModel(model);
        save();
    }//GEN-LAST:event_jComboBoxDeviceActionPerformed
    
    private void save(){
        if (enableSaving){
            int platformIdx = jComboBoxPlatform.getSelectedIndex();
            int deviceIdx = jComboBoxDevice.getSelectedIndex();
            AVM_Properties.setProperty(AVM_Properties.Name.CL_platform_Index, String.valueOf(platformIdx));
            AVM_Properties.setProperty(AVM_Properties.Name.CL_device_Index, String.valueOf(deviceIdx));
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Device> jComboBoxDevice;
    private javax.swing.JComboBox<Platform> jComboBoxPlatform;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
