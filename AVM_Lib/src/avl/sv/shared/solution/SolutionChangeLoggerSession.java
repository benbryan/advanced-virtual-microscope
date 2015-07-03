package avl.sv.shared.solution;

import avl.sv.shared.study.*;
import avl.sv.shared.image.ImageReference;
import java.util.Timer;
import javax.websocket.Session;

public class SolutionChangeLoggerSession{
    public final avl.sv.shared.AVM_Session avmSession;
    public final int solutionID;
    public final SolutionChangeListener changeListener;
    public final Timer timer;
    public final long loggerID;    

    public SolutionChangeLoggerSession(avl.sv.shared.AVM_Session avmSession, int studyID, SolutionChangeListener changeListener, Timer timer, long loggerID) {
        this.avmSession = avmSession;
        this.solutionID = studyID;
        this.changeListener = changeListener;
        this.timer = timer;
        this.loggerID = loggerID;
    }

}

