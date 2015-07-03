package avl.sv.shared;

import java.awt.Component;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;

public class AVM_ProgressMonitor extends ProgressMonitor{
    AtomicInteger progress = new AtomicInteger();
    public AVM_ProgressMonitor(Component parentComponent, Object message, String note, int min, int max) {
        super(parentComponent, message, note, min, max);
        progress.set(0);        
    }
    @Override
    public void setProgress(int nv) {
        super.setProgress(nv); 
        progress.set(nv);
    }
    public void incProgress(){
        super.setProgress(progress.incrementAndGet());
    }
    public int getProgress(){
        return progress.get();
    }
}
