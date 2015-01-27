package net.argius.eclipse.proj_gen;

import java.io.*;
import java.util.Optional;
import java.util.Properties;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextInputControl;
import org.apache.commons.lang3.StringUtils;

public final class Utils {

    private Utils() {
        //
    }

    public static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Properties getPropertiesFromResource(Class<?> c, String name) {
        Properties p = new Properties();
        InputStream is = c.getResourceAsStream(name);
        if (is != null) {
            try {
                p.load(is);
            } catch (IOException e) {
                // ignore
            }
        }
        return p;
    }

    public static Optional<File> validDirectory(String pathString) {
        File f = new File(pathString);
        if (f.exists() && f.isDirectory()) {
            return Optional.of(f);
        }
        return Optional.empty();
    }

    public static String toCanonicalAbsolutePathString(String pathString) {
        File f = new File(pathString);
        try {
            return f.getAbsoluteFile().getCanonicalPath();
        } catch (IOException e) {
            // TODO IOException at File#getCanonicalPath
            e.printStackTrace();
        }
        return f.getAbsolutePath();
    }

    public static boolean setTextIfBlank(TextInputControl tc, String s) {
        if (tc.isEditable() && StringUtils.isBlank(tc.getText())) {
            tc.setText(s);
            return true;
        }
        return false;
    }

    public static boolean async(Runnable task) {
        Service<Boolean> service = newService(task);
        service.start();
        return true; // TODO service.getValue();
    }

    public static Service<Boolean> newService(Runnable task) {
        return new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        task.run();
                        return true;
                    }
                };
            }
        };
    }

}
