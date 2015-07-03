package avl.sv.server.solution;

import avl.sv.shared.AVM_ProgressMonitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlindProgressMonitor extends AVM_ProgressMonitor {

    private AtomicInteger progress = new AtomicInteger();
    public String message, note;
    public int min, max;
    private boolean closed = false;
    private long lastCheckedTime;
    private boolean canceled = false;
    
    public BlindProgressMonitor(String message, String note, int min, int max) {
        super(null, "", "", 0, 0);
        this.message = message;
        this.note = note;
        this.min = min;
        this.max = max;
        lastCheckedTime = new Date().getTime();
        progress.set(0);
    }
    
    public int getProgress(){
        lastCheckedTime = new Date().getTime();
        return progress.get();
    }
    
    public long getLastCheckedTime(){
        return lastCheckedTime;
    }

    @Override
    public void setProgress(int nv) {
        progress.set(nv);
    }

    @Override
    public void incProgress() {
        progress.incrementAndGet();
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
    
    public void cancel(){
        this.canceled = true;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setMaximum(int m) {
        max = m;
    }

    @Override
    public int getMaximum() {
        return max;
    }

    @Override
    public void setMinimum(int m) {
        min = m;
    }
    
    boolean isClosed() {
        return closed;
    }
    
    public String toXML(){
        Properties p = new Properties();
        p.setProperty("min", String.valueOf(min));
        p.setProperty("max", String.valueOf(max));
        p.setProperty("progress", String.valueOf(getProgress()));
        p.setProperty("note", note);
        p.setProperty("message", message);
        p.setProperty("closed", String.valueOf(isClosed()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            p.storeToXML(bos, null);
        } catch (IOException ex) {
            Logger.getLogger(BlindProgressMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return "error: failed to get progress";
        }
        return bos.toString();
    }
    
}
