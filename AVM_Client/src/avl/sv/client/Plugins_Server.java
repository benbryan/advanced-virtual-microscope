package avl.sv.client;

import avl.sv.client.study.StudiesSourcePort;
import avl.sv.client.image.PluginImageFile;
import avl.sv.client.image.PluginImage;
import avl.sv.client.image.ImagesSourcePort;
import avl.sv.client.image.ImageManagerPort;
import avl.sv.client.study.PluginStudy;
import static avl.sv.client.AdvancedVirtualMicroscope.setStatusText;
import avl.sv.client.solution.PluginSolution;
import avl.sv.shared.solution.SolutionsSource;
import avl.sv.client.solution.SolutionsSourcePort;
import avl.sv.server.LoginPort;
import avl.sv.server.LoginPort_Service;
import avl.sv.server.images.Images;
import avl.sv.server.images.Images_Service;
import avl.sv.server.images.secure.ImagesSecure;
import avl.sv.server.images.secure.ImagesSecure_Service;
import avl.sv.server.solution.SolutionPort;
import avl.sv.server.solution.SolutionPort_Service;
import avl.sv.server.study.StudyPort;
import avl.sv.server.study.StudyPort_Service;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.study.StudiesSource;
import com.sun.xml.internal.ws.client.BindingProviderProperties;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import oracle.kv.impl.security.login.SessionId;

public class Plugins_Server {
    
    public static URL getloginPortLocation(String url) throws MalformedURLException{
        return new URL("https://" + url + ":443/AVM_Server/LoginPort?wsdl");
    }
    
    private static void addIdandTimeouts(BindingProvider bindingProvider, String uniqueID){
        Map<String, Object> req_ctx = bindingProvider.getRequestContext();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("avm_session_id", Collections.singletonList(uniqueID));
        req_ctx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        req_ctx.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        req_ctx.put(BindingProviderProperties.CONNECT_TIMEOUT, 3000);
        req_ctx.put(BindingProviderProperties.REQUEST_TIMEOUT, 3000);
        req_ctx.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
    }
    
    public static AVM_Plugin[] login(String username, String password, String url) throws MalformedURLException {
        URL loginPortLocation = getloginPortLocation(url);
        URL studyPortLocation = new URL("https://" + url + ":443/AVM_Server/StudyPort?wsdl");
        URL solutionPortLocation = new URL("https://" + url + ":443/AVM_Server/SolutionPort?wsdl");
        URL imagesPortLocaion = new URL("http://" + url + ":80/AVM_Server/Images?wsdl");
        URL imagesSecurePortLocaion = new URL("https://" + url + ":443/AVM_Server/ImagesSecure?wsdl");

        final LoginPort loginPort = getTempLoginPort(loginPortLocation);
        if (loginPort == null) {
            return null;
        }
        String loginResult;
        try {
            loginResult = loginPort.login(username, password);
        } catch (Exception ex) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(bos));
            String err = bos.toString();
            setStatusText("Failed to login to server at: " + loginPortLocation + " with " + err, 0);
            return null;
        }
        if ((loginResult == null) || (loginResult.contains("error:"))) {
            if (loginResult == null) {
                JOptionPane.showMessageDialog(null, "Failed to connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, loginResult.replaceFirst("error:", ""), "Error", JOptionPane.ERROR_MESSAGE);
            }
            setStatusText("Failed to login to server at: " + loginPortLocation + "because " + loginResult, 0);
            return null;
        }
        AVM_Session avmSession = new AVM_Session(username, loginResult);

        
        final ScheduledExecutorService worker = Executors.newScheduledThreadPool(1);
        worker.scheduleAtFixedRate(() -> {
            if (loginPort != null) {
                try {
                    loginPort.keepAlive();
                } catch (WebServiceException ex) {
                }
            }
        }, 120, 120, TimeUnit.SECONDS);
       
        
        ImagesSecure imagesSecurePort = new ImagesSecure_Service(imagesSecurePortLocaion).getImagesSecurePort();       
        Images imagesPort = new Images_Service(imagesPortLocaion).getImagesPort();
        StudyPort studyPort = new StudyPort_Service(studyPortLocation).getStudyPortPort();
        SolutionPort solutionPort = new SolutionPort_Service(solutionPortLocation).getSolutionPortPort();
        
        addIdandTimeouts((BindingProvider) loginPort,       avmSession.sessionID);
        addIdandTimeouts((BindingProvider) imagesPort,      avmSession.sessionID);
        addIdandTimeouts((BindingProvider) imagesSecurePort, avmSession.sessionID);
        addIdandTimeouts((BindingProvider) studyPort,       avmSession.sessionID);
        addIdandTimeouts((BindingProvider) solutionPort,    avmSession.sessionID);

        ImagesSourcePort imagesSource = new ImagesSourcePort(imagesPort, imagesSecurePort);
        StudiesSource studiesSource = new StudiesSourcePort(imagesSource, studyPort, avmSession);
        SolutionsSource solutionsSource = new SolutionsSourcePort(username, imagesSource, studiesSource, solutionPort, avmSession);
        
        PluginImageFile pluginImageFile = new PluginImageFile();
        PluginImage pluginImage = new PluginImage(imagesSource){
            @Override
            public ImageManager getImageManager(ImageReference imageReference) {
                return new ImageManagerPort(imageReference, imagesPort, imagesSecurePort);
            }
        };
        PluginStudy studyPlugin = new PluginStudy(username, studiesSource);
        PluginSolution pluginSolution = new PluginSolution(username, solutionsSource);
        
        AVM_Plugin plugins[] = new AVM_Plugin[]{pluginImage, studyPlugin, pluginSolution};       
        return plugins;
    }

    public static LoginPort getTempLoginPort(URL loginPortLocation) {
        try {
            return new LoginPort_Service(loginPortLocation).getLoginPortPort();
        } catch (WebServiceException ex) {
            String str = "Failed to connect to the server at " + loginPortLocation;
            JOptionPane.showMessageDialog(null, str);
            AdvancedVirtualMicroscope.setStatusText(str, 5000);
            return null;
        }
    }


}
