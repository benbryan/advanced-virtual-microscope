package avl.sv.client.study;

import avl.sv.client.image.ImageManagerPort;
import avl.sv.client.image.ImagesSourcePort;
import avl.sv.server.study.StudyPort;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.study.AnnotationSet;
import avl.sv.shared.Permissions;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageManagerSet;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageReferenceSet;
import avl.sv.shared.image.ImagesSource;
import avl.sv.shared.study.StudyChangeEvent;
import avl.sv.shared.study.StudyChangeListener;
import avl.sv.shared.study.StudySource;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.EndpointConfig;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.xml.ws.BindingProvider;
import sun.net.ftp.FtpDirEntry;

public final class StudySourcePort extends StudySource {

    private final ImagesSourcePort imagesSource;
    private final StudyPort port;
    ArrayList<AnnotationSet> annotationSets = new ArrayList<>();
    HashMap<ImageReference, StudyChangeListener> studyChangeListeners = new HashMap<>();
    private final Permissions permissions;
    private String name = null;    

    public StudySourcePort(ImagesSourcePort imagesSource, StudyPort port, int id, AVM_Session avmSession) {
        super(id, avmSession);
        this.imagesSource = imagesSource;
        this.port = port;
        this.permissions = Permissions.valueOf(port.getPermissions(id, avmSession.username));
    }
    
    public StudySourcePort(ImagesSourcePort imagesSource, StudyPort port, int id, AVM_Session avmSession, Permissions permissions) {
        super(id, avmSession);
        this.imagesSource = imagesSource;
        this.port = port;
        this.permissions = permissions;
    }

    @Override
    public StudySourcePort cloneStudy(String cloneName, String adminName) {
        String result = cloneStudy(cloneName);
        if (result == null) {
            return null;
        }
        try {
            int id = Integer.valueOf(result);
            StudySourcePort clone = new StudySourcePort(imagesSource, port, id, avmSession);
            try {
                clone.setName(cloneName);
            } catch (PermissionDenied ex) {
                Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
            }
            return clone;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public void addImage(ImageManager imageManager) {
        StudyChangeEvent event = new StudyChangeEvent(imageManager.imageReference, studyID, -1, -1, StudyChangeEvent.Type.AddImage, avmSession.username, "");
        addChanges(event);
        try {
            super.addImage(imageManager);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void removeImage(ImageReference imageReference) {
        StudyChangeEvent event = new StudyChangeEvent(imageReference, studyID, -1, -1, StudyChangeEvent.Type.RemoveImage, avmSession.username, "");
        addChanges(event);
        try {
            super.removeImage(imageReference);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected String cloneStudy(String cloneName) {
        return port.cloneStudy(studyID, cloneName);
    }

    @Override
    public ImagesSource getImagesSource() {
        return imagesSource;
    }

    @Override
    public String getAnnotationSetXML(ImageReference imageReference) {
        return port.getAnnotationSetXML(studyID, imageReference.toXML());
    }

    @Override
    public String getImageReferenceSetsXML() {
        return port.getImageReferenceSetsXML(studyID);
    }

    @Override
    public String setDescription(String description) throws PermissionDenied{
        return port.setDescription(studyID, description);
    }

    @Override
    public String getDescription() {
        return port.getDescription(studyID);
    }

    @Override
    public String delete() {
        return port.delete(studyID);
    }

    @Override
    public Permissions getPermissions() {
        return permissions;
    }

    @Override
    public String setName(String newName) throws PermissionDenied {
        String result = port.setName(studyID, newName);
        if (!result.startsWith("error")) {
            super.setUserObject(newName);
            this.name = newName;
        }
        return result;
    }

    public void setNameQuiet(String name){
        this.name = name;
    }
    
    @Override
    public String getUsers() {
        return port.getUsers(studyID);
    }

    @Override
    public Permissions getPermissions(String username) {
        return Permissions.valueOf(port.getPermissions(studyID, username));
    }

    @Override
    public String setPermission(String username, Permissions permission) {
        return port.setPermissions(studyID, username, permission.name());
    }

    @Override
    public void clearROI_Folder(ImageReference imageReference, long folderID) {
        StudyChangeEvent event = new StudyChangeEvent(imageReference, studyID, folderID, -1, StudyChangeEvent.Type.Delete, avmSession.username, "");
        addChanges(event);
    }

    @Override
    public void clearROI(ImageReference imageReference, long folderID, long roiID) {
        StudyChangeEvent event = new StudyChangeEvent(imageReference, studyID, folderID, roiID, StudyChangeEvent.Type.Delete, avmSession.username, "");
        addChanges(event);
    }

    @Override
    public ImageManager getImageManager(ImageReference imageReference) {
        return new ImageManagerPort(imageReference, imagesSource.imagesPort, imagesSource.imagesSecurePort);
    }

    @Override
    public ArrayList<ImageManagerSet> getImageManagerSets() {
        String xml = getImageReferenceSetsXML();
        if (xml.equals(Permissions.PERMISSION_DENIED)) {
            return null;
        }
        ArrayList<ImageReferenceSet> imageReferenceSets = ImageReferenceSet.parse(xml);
        return imagesSource.convert(imageReferenceSets);
    }

    private Session studyPortChangeLoggerSession = null;
    private Timer pingTimer;
    private static final long pingTimerPeroid = 1 * 1000;
    private ExecutorService studyPortChangeLoggerSessionExecutor = null;

    private boolean initStudyPortChangeLoggerSession() {
        if ((studyPortChangeLoggerSession != null) && studyPortChangeLoggerSession.isOpen()) {
            return true;
        }
        if (studyPortChangeLoggerSessionExecutor != null) {
            studyPortChangeLoggerSessionExecutor.shutdown();
        }
        studyPortChangeLoggerSessionExecutor = Executors.newSingleThreadExecutor();

        Future<Exception> result = studyPortChangeLoggerSessionExecutor.submit(() -> {
            try {
                BindingProvider bp = (BindingProvider) port;
                Map<String, Object> req_ctx = bp.getRequestContext();
                String changeMonitorURL = (String) req_ctx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
                changeMonitorURL = changeMonitorURL.replace("http://", "ws://").replace("https://", "wss://");
                //TODO: re enable https for monitor
                changeMonitorURL = changeMonitorURL.replace("wss://", "ws://").replace(":443", ":80");
                changeMonitorURL = changeMonitorURL + "/ChangeMonitor"
                        + "/" + avmSession.sessionID
                        + "/" + String.valueOf(studyID);
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                StudyChangeMonitorClient changeMonitor = new StudyChangeMonitorClient((StudyChangeEvent event) -> {
                    StudyChangeListener listener = studyChangeListeners.get(event.imageReference);
                    if (listener != null) {
                        listener.studyChanged(event);
                    }
                }, connectionListener);
                studyPortChangeLoggerSession = container.connectToServer(changeMonitor, new URI(changeMonitorURL));
                return null;
            } catch (Exception ex) {
                return ex;
            }
        });
        try {
            Exception ex = result.get(3, TimeUnit.SECONDS);
            if (ex != null){
                Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return studyPortChangeLoggerSession != null && studyPortChangeLoggerSession.isOpen();
            } catch (InterruptedException | ExecutionException | TimeoutException ex1) {
            Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex1);
        }
        return false;
    }

    ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onPong(PongMessage pongMessage, Session session) {
            LongBuffer lb = pongMessage.getApplicationData().asLongBuffer();
            if (lb.remaining() < 1) {
                return;
            }
            long time = lb.get();
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            if (pingTimer != null) {
                pingTimer.cancel();
            }
            pingTimer = new Timer();
            pingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!session.isOpen()) {
                        return;
                    }
                    try {
                        ByteBuffer bb = ByteBuffer.allocate(8);
                        bb.putLong(new Date().getTime());
                        session.getBasicRemote().sendPong((ByteBuffer) bb.flip());
                    } catch (IOException | IllegalArgumentException ex) {
                        System.out.println("session.getBasicRemote().sendPong((ByteBuffer) bb.flip());");
                        Logger.getLogger(StudyChangeMonitorClient.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("/session.getBasicRemote().sendPong((ByteBuffer) bb.flip());");
                    }
                }
            }, pingTimerPeroid, pingTimerPeroid);
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            if (pingTimer != null) {
                pingTimer.cancel();
                pingTimer = null;
            }
            if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
                initStudyPortChangeLoggerSession();
            }
        }

        @Override
        public void onError(Session session, Throwable t) {
            System.out.println("StudyChangeMonitorClient throwing");
            Logger.getLogger(StudyChangeMonitorClient.class.getName()).log(Level.WARNING, null, t);
            System.out.println("/StudyChangeMonitorClient throwing");
        }
    };

    @Override
    public void addStudyChangeListener(ImageReference imageReference, StudyChangeListener listener) {
        if (studyPortChangeLoggerSession == null) {
            initStudyPortChangeLoggerSession();
            if (studyPortChangeLoggerSession == null) {
                return;
            }
        }
        StudyChangeEvent event = new StudyChangeEvent(imageReference, studyID, -1, -1, StudyChangeEvent.Type.AddListener, avmSession.username, "");
        studyPortChangeLoggerSession.getAsyncRemote().sendObject(event);
        studyChangeListeners.put(imageReference, listener);
    }

    @Override
    public void close() {
        try {
            if ((studyPortChangeLoggerSession != null) && studyPortChangeLoggerSession.isOpen()) {
                studyPortChangeLoggerSession.close();
            }
            studyPortChangeLoggerSession = null;
        } catch (IOException ex) {
            Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.close(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeStudyChangeListener(StudyChangeListener listener) {
        for (Map.Entry<ImageReference, StudyChangeListener> entry : studyChangeListeners.entrySet()) {
            if (entry.getValue().equals(listener)) {
                studyChangeListeners.remove(entry.getKey());
                StudyChangeEvent event = new StudyChangeEvent(entry.getKey(), studyID, -1, -1, StudyChangeEvent.Type.RemoveListener, avmSession.username, "");
                addChanges(event);
            }
        }
        if (studyChangeListeners.isEmpty()) {
            try {
                studyPortChangeLoggerSession.close();
                studyPortChangeLoggerSession = null;
            } catch (IOException ex) {
                Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void removeAllStudyChangeListeners() {
        try {
            if ((studyPortChangeLoggerSession != null) && studyPortChangeLoggerSession.isOpen()) {
                studyPortChangeLoggerSession.close();
            }
            studyPortChangeLoggerSession = null;
        } catch (IOException ex) {
            Logger.getLogger(StudySourcePort.class.getName()).log(Level.SEVERE, null, ex);
        }
        studyChangeListeners.clear();
    }

    @Override
    synchronized public void addChanges(StudyChangeEvent event) {
        initStudyPortChangeLoggerSession();
        if (studyPortChangeLoggerSession.isOpen()){
            studyPortChangeLoggerSession.getAsyncRemote().sendObject(event);
        } else {
            JOptionPane.showMessageDialog(null, "Failed to send changes to server");            
        }
    }

    @Override
    public String getName() {
        if (this.name == null){
            name = port.getName(studyID);            
            super.setUserObject(name);
        }
        return name;
    }

}
