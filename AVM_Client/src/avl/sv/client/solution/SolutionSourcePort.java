package avl.sv.client.solution;

import avl.sv.client.image.ImagesSourcePort;
import avl.sv.server.solution.SolutionPort;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.Permissions;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.solution.Solution;
import avl.sv.shared.solution.xml.SolutionXML_Parser;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.solution.SolutionChangeEvent;
import avl.sv.shared.solution.SolutionChangeListener;
import avl.sv.shared.study.StudySource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.xml.sax.SAXException;

public class SolutionSourcePort extends SolutionSource {

    private Permissions permissions = null;
    
    public static SolutionSource get(ImagesSourcePort imagesSource, SolutionPort solutionPort, StudySource studySource, int id, AVM_Session avmSession) throws PermissionDenied {
        SolutionSourcePort solutionSourcePort = new SolutionSourcePort(imagesSource, solutionPort, studySource, id, avmSession);
        if (solutionSourcePort.getPermissions().canRead()){
            return solutionSourcePort;
        }
        throw new PermissionDenied();
    }
    
    public static SolutionSource get(ImagesSourcePort imagesSource, SolutionPort solutionPort, StudySource studySource, int id, AVM_Session avmSession, Permissions permissions) {
        SolutionSourcePort solutionSourcePort = new SolutionSourcePort(imagesSource, solutionPort, studySource, id, avmSession);
        solutionSourcePort.permissions = permissions;
        return solutionSourcePort;
    }
    
    private final SolutionPort solutionPort;
    private final StudySource studySource;
    private final ImagesSourcePort imagesSource;
    Session solutionPortChangeLoggerSession = null;
    private final AVM_Session avmSession;
   
    private SolutionSourcePort(ImagesSourcePort imagesSource, SolutionPort port, StudySource studySource, int id, AVM_Session avmSession) {
        super(id);
        this.solutionPort = port;
        this.imagesSource = imagesSource;
        this.studySource = studySource;
        this.avmSession = avmSession;
    }
    
    @Override
    public String setSolution(String xml) {
        return solutionPort.set(solutionId, xml);
    }

    @Override
    public Solution getSolution() {
        try {
            String xml = solutionPort.get(solutionId);
            if (xml.contains("error:")) {
                JOptionPane.showMessageDialog(null, xml, "Error getting solution from server", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            SolutionXML_Parser parser = new SolutionXML_Parser();
            Solution solution = parser.parse(xml);
            return solution;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SolutionSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean exists(SolutionPort port, String name) {
        return port.get(solutionId) != null;
    }

    @Override
    public String delete() {
        return solutionPort.delete(solutionId);
    }

    @Override
    public String setDescription(String description) {
        return solutionPort.setDescription(solutionId, description);
    }

    @Override
    public String getDescription() {
        return solutionPort.getDescription(solutionId);
    }

    @Override
    public Permissions getPermissions() {
        if (permissions == null){
            permissions = Permissions.valueOf(solutionPort.getPermissions(solutionId, null));
        }
        return permissions;
    }

    @Override
    public String getUsers() {
        return solutionPort.getUsers(solutionId);
    }

    @Override
    public String setPermissions(String username, Permissions permission) {
        return solutionPort.setPermissions(solutionId, username, permission.name());
    }

    @Override
    public Permissions getPermissions(String username) {
        return Permissions.valueOf(solutionPort.getPermissions(solutionId, username));
    }

    @Override
    public Properties getFeatures(ImageReference imageReference, int tileDim, int tileWindowDim, String featureGeneratorClassName, String featureNames[]){
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(featureNames));
        String features = solutionPort.getFeatures(solutionId, imageReference.toXML(), tileDim, tileWindowDim, featureGeneratorClassName, list);
        Properties p = new Properties();
        try {
            p.loadFromXML(new ByteArrayInputStream(features.getBytes()));
        } catch (IOException ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return p;
    }

    @Override
    public String generateInDatabase(ImageReference imageReference, int tileDim, int tileWindowDim, String featureGeneratorClassName, String featureNames[]){
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(featureNames));
        return solutionPort.generateInDatabase(solutionId, imageReference.toXML(), tileDim, tileWindowDim, featureGeneratorClassName, list);
    }    
    
    @Override
    public SolutionSource cloneSolution(String cloneName) {
        String result = solutionPort.clone(solutionId, cloneName);
        if ((result == null) || result.startsWith("error:")){
            JOptionPane.showMessageDialog(null, result, "Failed to clone the solution", JOptionPane.ERROR_MESSAGE);
            return null;
        } else {
            return new SolutionSourcePort(imagesSource, solutionPort, studySource, Integer.parseInt(result), avmSession);
        }
    }
    
    public String trainOnServer(){
        return solutionPort.trainClassifier(solutionId);
    }

    public Properties getProgress(long monitorID){
        try {
            String temp = solutionPort.getProgress(monitorID);
            Properties p = new Properties();
            p.loadFromXML(new ByteArrayInputStream(temp.getBytes()));
            return p;
        } catch (Exception ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void cancelMonitor(long monitorID){
        try{
        solutionPort.cancelMonitor(monitorID);
        } catch (Exception ex){
            
        }
    }
    

    private void initSolutionPortChangeLoggerSession() {
        try {
            BindingProvider bp = (BindingProvider) solutionPort;
            Map<String, Object> req_ctx = bp.getRequestContext();
            String changeMonitorURL = (String) req_ctx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            changeMonitorURL = changeMonitorURL.replace("http://", "ws://").replace("https://", "wss://");
            //TODO: re enable https for monitor
            changeMonitorURL = changeMonitorURL.replace("wss://", "ws://").replace(":443", ":80");
            changeMonitorURL = changeMonitorURL + "/ChangeMonitor"
                    + "/" + avmSession.sessionID
                    + "/" + String.valueOf(solutionId);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            SolutionChangeMonitorClient changeMonitor = new SolutionChangeMonitorClient(new SolutionChangeListener() {
                @Override
                public void solutionChanged(SolutionChangeEvent event) {
                    for (SolutionChangeListener listener:solutionChangeListeners){
                        listener.solutionChanged(event);
                    }
                }
            });
            solutionPortChangeLoggerSession = container.connectToServer(changeMonitor, new URI(changeMonitorURL));
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void addSolutionChangeListener(SolutionChangeListener listener) {
        if (solutionPortChangeLoggerSession == null){
            initSolutionPortChangeLoggerSession();
            if (solutionPortChangeLoggerSession == null){
                return;
            }
        }    
        try {
            solutionPortChangeLoggerSession.getBasicRemote().sendText("start:");
            solutionChangeListeners.add(listener);
        } catch (IOException ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    ArrayList<SolutionChangeListener> solutionChangeListeners = new ArrayList<>();

    @Override
    public void close() {
        try {
            solutionPortChangeLoggerSession.close();
        } catch (IOException ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   

    @Override
    public void removeSolutionChangeListener(SolutionChangeListener listener) {
        if (solutionPortChangeLoggerSession == null){
            return;
        }
        try {
            solutionChangeListeners.remove(listener);
            if (solutionChangeListeners.isEmpty()){
                solutionPortChangeLoggerSession.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            solutionPortChangeLoggerSession = null; 
        }
    }

    @Override
    public void removeAllSolutionChangeListeners() {
        try {
            solutionPortChangeLoggerSession.close();
            solutionPortChangeLoggerSession = null;
        } catch (IOException ex) {
            Logger.getLogger(SolutionSourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
        solutionChangeListeners.clear();
    }

    @Override
    public StudySource getStudySource() {
        return studySource;
    }
    
    private String name = null; 

    @Override
    public String getName() {
        if (name == null){
            name =  solutionPort.getName(solutionId);
        }
        return name;
    }

    @Override
    public void setName(String name) {
        solutionPort.setName(solutionId, name);
        this.name = name;
        super.setUserObject(name);
    }

    @Override
    public void setNameQuiet(String name) {
        this.name = name;
        super.setUserObject(name);
    }
    
}
