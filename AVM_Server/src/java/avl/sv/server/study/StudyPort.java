package avl.sv.server.study;

import avl.sv.shared.study.StudySourceKVStore;
import avl.sv.server.SessionManagerServer;
import avl.sv.shared.image.ImageReference;
import avl.sv.shared.MessageStrings;
import avl.sv.shared.Permissions;
import avl.sv.shared.AVM_Session;
import avl.sv.shared.PermissionDenied;
import avl.sv.shared.image.ImageManagerKVStore;

import avl.sv.shared.study.StudiesSourceKVStore;
import avl.sv.shared.study.StudySource;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "StudyPort")
public class StudyPort {
    
    @Resource
    private WebServiceContext wsContext;
    private static final SessionManagerServer sessionManager = SessionManagerServer.getInstance();
    private AVM_Session getSession(){
        return sessionManager.getSession(SessionManagerServer.getSessionID(wsContext));
    }
    
    @WebMethod(operationName = "getAnnotationSetXML")
    public String getAnnotationSetXML(@WebParam(name = "id") final int id,
                                    @WebParam(name = "ImageReferenceXML") final String imageReferenceXML) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }

        ImageReference imageReference = new ImageReference(imageReferenceXML);
        return studySource.getAnnotationSetXML(imageReference);
    }

    @WebMethod(operationName = "getList")
    public String getList() {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudiesSourceKVStore studiesSourceKVStore = new StudiesSourceKVStore(session);
        return studiesSourceKVStore.getStudySourcesString();
    }

    @WebMethod(operationName = "create")
    public String create(@WebParam(name = "name") final String name) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource = StudySourceKVStore.create(session, name);
        if (studySource == null) {
            return MessageStrings.FAILED_TO_CREATE_STUDY;
        } else {
            return String.valueOf(studySource.studyID);
        }
    }
    
    @WebMethod(operationName = "setDescription")
    public String setDescription(@WebParam(name = "id") final int id,
            @WebParam(name = "description") final String description) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
            return studySource.setDescription(description);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getDescription")
    public String getDescription(@WebParam(name = "id") final int id) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
            return studySource.getDescription();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getName")
    public String getName(@WebParam(name = "id") final int id) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        return studySource.getName();
    }    
    
    @WebMethod(operationName = "delete")
    public String delete(@WebParam(name = "id") final int id) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
            return studySource.delete();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getImageReferenceSetsXML")
    public String getImageReferenceSetsXML(@WebParam(name = "id") final int id) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
            return studySource.getImageReferenceSetsXML();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "setPermissions")
    public String setPermissions(@WebParam(name = "id") final int id,
            @WebParam(name = "session.username") final String targetUsername,
            @WebParam(name = "permission") final String newPermission) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        try {
            StudySource studySource = StudySourceKVStore.get(session, id);
            return studySource.setPermission(targetUsername, Permissions.valueOf(newPermission));
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getPermissions")
    public String getPermissions(@WebParam(name = "id") final int id,
            @WebParam(name = "targerUsername") String targerUsername) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource studySource;
        try {
            studySource = StudySourceKVStore.get(session, id);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        if ((targerUsername == null) || (targerUsername.isEmpty())) {
            return studySource.getPermissions().name();
        }
        try {
            return studySource.getPermissions(targerUsername).name();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "getUsers")
    public String getUsers(@WebParam(name = "id") final int id) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        try {
            StudySource studySource = StudySourceKVStore.get(session, id);
            return studySource.getUsers();
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "setName")
    public String setName(@WebParam(name = "id") final int id,
            @WebParam(name = "newName") final String newName) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        try {
            StudySource studySource = StudySourceKVStore.get(session, id);
            return studySource.setName(newName);
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
    }

    @WebMethod(operationName = "cloneStudy")
    public String cloneStudy( @WebParam(name = "id") final int id,
                              @WebParam(name = "cloneName") final String cloneName) {
        AVM_Session session = getSession();
        if (session == null) { return MessageStrings.SESSION_EXPIRED; }
        StudySource originalStudy;
        try {
            originalStudy = StudySourceKVStore.get(session, id);
            if (originalStudy == null){ return MessageStrings.PERMISSION_DENIED;  } 
        } catch (PermissionDenied ex) {
            Logger.getLogger(StudyPort.class.getName()).log(Level.WARNING, null, ex);
            return MessageStrings.PERMISSION_DENIED;
        }
        StudySource clonedStudy = originalStudy.cloneStudy(cloneName, session.username);
        return String.valueOf(clonedStudy.studyID);    
    }
}
