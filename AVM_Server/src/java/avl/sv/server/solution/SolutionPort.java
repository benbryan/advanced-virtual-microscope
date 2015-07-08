package avl.sv.server.solution;

import avl.sv.server.SessionManagerServer;
import avl.sv.server.study.StudyPort;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.image.ImageSource;
import avl.sv.shared.image.ImageSourceKVStore;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.Permissions;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.ProgressBarForegroundPainter;
import avl.sv.shared.image.ImageManager;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.solution.FeatureGenerateTask;
import avl.sv.shared.solution.FeatureGeneratorTaskManager;
import avl.sv.shared.solution.SampleSetClass;
import avl.sv.shared.solution.Solution;
import avl.sv.shared.solution.SolutionSource;
import avl.sv.shared.solution.SolutionSourceKVStore;
import avl.sv.shared.solution.SolutionsSourceKVStore;
import avl.sv.shared.study.StudySource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.swing.UIManager;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.WebServiceContext;
import weka.core.Instances;

@WebService(serviceName = "SolutionPort")
public class SolutionPort {

    FeatureGeneratorTaskManager generator = FeatureGeneratorTaskManager.getInstance();
    HashMap<Long, BlindProgressMonitor> progressMonitors = new HashMap<>();
    Random random = new Random();

    @Resource
    private WebServiceContext wsContext;
    private static SessionManagerServer sessionManager = SessionManagerServer.getInstance();

    private AVM_Session getSession() {
        return sessionManager.getSession(sessionManager.getSessionID(wsContext));
    }

    @WebMethod(operationName = "get")
    public String get(@WebParam(name = "ID") int id) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        return solutionSource.getSolutionXML();
    }

    /**
     *
     * @param id solutionId of target solution
     * @param xml xml version of solution
     * @return return an error message or the current version (Long as string)
     * of the solution
     */
    @WebMethod(operationName = "set")
    public String set(@WebParam(name = "ID") final int id,
            @WebParam(name = "xml") final String xml) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        try {
            SolutionSourceKVStore solutionSource = SolutionSourceKVStore.get(session, id);
            return solutionSource.setSolution(xml);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getSolutionsList")
    public String getSolutionsList() {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        return new SolutionsSourceKVStore(session).getSolutionsList();
    }

    @WebMethod(operationName = "setDescription")
    public String setDescription(@WebParam(name = "ID") final int id,
            @WebParam(name = "description") final String description) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        try {
            SolutionSourceKVStore solutionSource = SolutionSourceKVStore.get(session, id);
            return solutionSource.setDescription(description);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "setName")
    public String setName(@WebParam(name = "ID") final int id,
            @WebParam(name = "name") final String name) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        try {
            solutionSource.setName(name);
            return MessageStrings.SUCCESS;
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getDescription")
    public String getDescription(@WebParam(name = "ID") final int id) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        return solutionSource.getDescription();
    }

    @WebMethod(operationName = "getName")
    public String getName(@WebParam(name = "ID") final int id) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        return solutionSource.getName();
    }

    @WebMethod(operationName = "create")
    public String create(@WebParam(name = "name") final String name) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore ss;
        try {
            ss = SolutionSourceKVStore.create(session, name);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        if (ss == null) {
            return MessageStrings.FAILED_TO_CREATE_SOLUTION;
        } else {
            return String.valueOf(ss.solutionId);
        }
    }

    @WebMethod(operationName = "delete")
    public String delete(@WebParam(name = "ID") final int id) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
            return solutionSource.delete();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "trainClassifier")
    public String trainClassifier(@WebParam(name = "ID") final int id) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        try {
            if (!solutionSource.getPermissions(session.username).canModify()) {
                return MessageStrings.PERMISSION_DENIED;
            }
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionPort.class.getName()).log(Level.SEVERE, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        final long pmID = random.nextLong();
        FeatureGeneratorTaskManager.onHold.set(true);
        Executors.newSingleThreadExecutor().submit(() -> {
            Solution solutionHold = solutionSource.getSolution();
            UIManager.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarForegroundPainter());
            final BlindProgressMonitor pm = new BlindProgressMonitor("Collection image references", "", 0, 1000000);
            progressMonitors.put(pmID, pm);
            pm.setProgress(1);
            pm.setNote("Waiting in queue");
            while (FeatureGeneratorTaskManager.isBusy()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SolutionPort.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            pm.setNote("Collecting image sources");
            ArrayList<ImageSource> imageSources = getRequiredImageSources(solutionSource);
            pm.setProgress(3);
            pm.setNote("Generating Features");
            try {
                Instances instances;

                ArrayList<String> featureNames = new ArrayList<>();
                for (AbstractFeatureGenerator generator : solutionHold.getFeatureGenerators()) {
                    for (String name : generator.getFeatureNames()) {
                        featureNames.add(name);
                    }
                }

                ArrayList<String> classNames = new ArrayList<>();
                classNames.addAll(solutionHold.getClassifierClassNames().values());

                try {
                    instances = SampleSetClass.generateInstances(imageSources, solutionSource, pm);
                } catch (OutOfMemoryError ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    pm.setNote("error: Java heap space out of menory");
                    pm.close();
                    return;
                } catch (Throwable ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    pm.setNote("error: Failed to generate features");
                    pm.close();
                    return;
                }

                if (instances == null) {
                    if (!pm.isCanceled()) {
                        pm.setNote("Classifier generation canceled");
                        pm.close();
                    }
                }

                // Check if there are samples in each set
                long count[] = new long[instances.numClasses()];
                for (int i = 0; i < count.length; i++){
                    count[i] = 0;
                }
                for (int i =0; i < instances.numInstances(); i++){
                    count[(int)instances.instance(i).classValue()]++;
                }
                for (int i = 0; i < count.length; i++){
                    if (count[i] < 10) {
                        pm.setNote("error: Not enough samples collected");
                        pm.close();
                        return;
                    }
                }
                pm.setNote("Training classifier(s)");
                solutionHold.trainClassifiers(instances);
                solutionSource.setSolution(solutionHold);
                pm.setNote("Classifier trained, downloading now");
                pm.close();
            } catch (Throwable ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", ex);
                pm.setNote("error: Failed to generate the classifier(s)");
                pm.close();
            }
            FeatureGeneratorTaskManager.onHold.set(false);
        });
        return String.valueOf(pmID);
    }

    private static ArrayList<ImageSource> getRequiredImageSources(SolutionSource solutionSource) {
        StudySource study = solutionSource.getStudySource();
        ArrayList<ImageSource> imageSources = new ArrayList<>();
        for (ImageManager imageManager : study.getAllImageManagers()) {
            ImageSourceKVStore imageSource = new ImageSourceKVStore(imageManager.imageReference);
            imageSources.add(imageSource);
        }
        return imageSources;
    }

    @WebMethod(operationName = "setPermissions")
    public String setPermissions(@WebParam(name = "ID") final int id,
            @WebParam(name = "username") final String targetUsername,
            @WebParam(name = "permission") final String newPermission) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        try {
            SolutionSourceKVStore solutionSource = SolutionSourceKVStore.get(session, id);
            return solutionSource.setPermissions(targetUsername, Permissions.valueOf(newPermission));
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getPermissions")
    public String getPermissions(@WebParam(name = "ID") final int id,
            @WebParam(name = "targetUsername") String targetUsername) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        if ((targetUsername == null) || targetUsername.isEmpty() || session.username.equals(targetUsername.toLowerCase())) {
            return solutionSource.getPermissions().name();
        }
        try {
            return solutionSource.getPermissions(targetUsername).name();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getUsers")
    public String getUsers(@WebParam(name = "ID") final int id) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
            return solutionSource.getUsers();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getFeatures")
    public String getFeatures(@WebParam(name = "ID") final int id,
            @WebParam(name = "ImageReferenceXML") final String imageReferenceXML,
            @WebParam(name = "tileDim") final int tileDim,
            @WebParam(name = "tileWindowDim") final int tileWindowDim,
            @WebParam(name = "featureGeneratorClassName") final String featureGeneratorClassName,
            @WebParam(name = "featureNames") final String featureNamesIn[]
    ) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        ArrayList<String> featureNames = new ArrayList<>();
        featureNames.addAll(Arrays.asList(featureNamesIn));
        Properties features = new Properties();

        ImageReference imageReference = new ImageReference(imageReferenceXML);
        ArrayList<String> clone = new ArrayList<>();
        clone.addAll(featureNames);
        for (String featureName : clone) {
            byte feature[] = solutionSource.getFeatureRaw(imageReference, featureGeneratorClassName, featureName, tileDim, tileWindowDim);
            if (feature != null) {
                featureNames.remove(featureName);
                features.setProperty(featureName, DatatypeConverter.printBase64Binary(feature));
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            features.storeToXML(os, null);
        } catch (IOException ex) {
            Logger.getLogger(SolutionPort.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return new String(os.toByteArray());
    }

    @WebMethod(operationName = "generateInDatabase")
    public String generateInDatabase(@WebParam(name = "ID") final int id,
            @WebParam(name = "ImageReferenceXML") final String imageReferenceXML,
            @WebParam(name = "tileDim") final int tileDim,
            @WebParam(name = "tileWindowDim") final int tileWindowDim,
            @WebParam(name = "featureGeneratorClassName") final String featureGeneratorClassName,
            @WebParam(name = "featureNames") final String featureNamesIn[]
    ) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        SolutionSourceKVStore solutionSource;
        try {
            solutionSource = SolutionSourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        ArrayList<String> featureNames = new ArrayList<>();
        featureNames.addAll(Arrays.asList(featureNamesIn));
        Properties features = new Properties();
        ImageReference imageReference = new ImageReference(imageReferenceXML);
        ImageSourceKVStore imageSourceKVStore = new ImageSourceKVStore(imageReference);
        int numelSamples = imageSourceKVStore.getImageDimX()/tileDim*imageSourceKVStore.getImageDimY()/tileDim;
        if ((numelSamples*8)>5e6){
            return MessageStrings.TileDimTooSmallForDatabase;
        }
        
        ArrayList<String> clone = new ArrayList<>();
        clone.addAll(featureNames);
        for (String featureName : clone) {
            byte feature[] = solutionSource.getFeatureRaw(imageReference, featureGeneratorClassName, featureName, tileDim, tileWindowDim);
            if (feature != null) {
                featureNames.remove(featureName);
                features.setProperty(featureName, DatatypeConverter.printBase64Binary(feature));
            }
        }
        if ((featureNames.size() > 0)) {
            FeatureGeneratorTaskManager.getInstance().addTaskToQue(new FeatureGenerateTask(featureNames, tileDim, tileWindowDim, featureGeneratorClassName, imageReference));
        }
        return MessageStrings.SUCCESS;
    }

    @WebMethod(operationName = "clone")
    public String clone( @WebParam(name = "id") final int id, 
                         @WebParam(name = "cloneName") final String cloneName) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        SolutionSourceKVStore solutionSourceOriginal;
        try {
            solutionSourceOriginal = SolutionSourceKVStore.get(session, id);
            if (solutionSourceOriginal == null){ return MessageStrings.PERMISSION_DENIED;  } 
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        SolutionSourceKVStore clonedStudy;
        try {
            clonedStudy = solutionSourceOriginal.cloneSolution(cloneName);
        } catch (PermissionDenied ex) {
            Logger.getLogger(SolutionPort.class.getName()).log(Level.SEVERE, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        return String.valueOf(clonedStudy.solutionId);   
    }

    @WebMethod(operationName = "getProgress")
    public String getProgress(@WebParam(name = "progressMonitorID") final long progressMonitorID) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        BlindProgressMonitor pm = progressMonitors.get(progressMonitorID);
        return pm.toXML();
    }

    @WebMethod(operationName = "cancelMonitor")
    public String cancelMonitor(@WebParam(name = "progressMonitorID") final long progressMonitorID) {
        AVM_Session session = getSession();
        if (session == null) {
            return MessageStrings.SESSION_EXPIRED;
        }
        progressMonitors.remove(progressMonitorID).cancel();
        return "error: failed to get progress";
    }

}
