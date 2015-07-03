package avl.sv.shared.study;

import avl.sv.shared.image.ImageReference;
import java.util.Timer;
import javax.websocket.Session;

public class StudyChangeLoggerSession{
    public final avl.sv.shared.AVM_Session avmSession;
    public final int studyID;
    public final StudyChangeListener studyChangeListener;
    public final ImageReference imageReference;
    public final Timer timer;
    public final long loggerID;    

    public StudyChangeLoggerSession(avl.sv.shared.AVM_Session avmSession, int studyID, ImageReference imageReference, StudyChangeListener studyChangeListener, Timer timer, long loggerID) {
        this.avmSession = avmSession;
        this.studyID = studyID;
        this.studyChangeListener = studyChangeListener;
        this.imageReference = imageReference;
        this.timer = timer;
        this.loggerID = loggerID;
    }

}

