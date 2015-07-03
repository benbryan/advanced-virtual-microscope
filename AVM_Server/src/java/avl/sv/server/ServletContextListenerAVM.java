package avl.sv.server;

import avl.sv.server.images.UploadAuthManager;
import avl.sv.shared.AVM_Properties;
import avl.sv.shared.solution.FeatureGeneratorTaskManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextListenerAVM implements ServletContextListener {

    ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent e) {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                SessionManagerServer.getInstance().purgeDayOldSessions();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", ex);
            }
        }, 6, 6, TimeUnit.HOURS);

        int timeout = Integer.parseInt(AVM_Properties.getProperty(AVM_Properties.UPLOAD_SESSION_TIMEOUT_MINUTES));
        executor.scheduleAtFixedRate(() -> {
            try {
                UploadAuthManager.purgeExpiredAuths();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failure while purging expired auths", ex);
            }
        }, timeout, timeout, TimeUnit.MINUTES);

        int featureGeneratorTaskManagerDelay = 5;
        executor.scheduleAtFixedRate(() -> {
            try {
                FeatureGeneratorTaskManager.getInstance().start();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failure while purging expired auths", ex);
            }
        }, featureGeneratorTaskManagerDelay, featureGeneratorTaskManagerDelay, TimeUnit.SECONDS);

    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

    }

    
    
}
