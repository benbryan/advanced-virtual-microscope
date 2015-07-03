package avl.sv.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM_ClassLoader extends ClassLoader {

    final ArrayList<String> targetClassNames;

    public AVM_ClassLoader(ClassLoader parent, ArrayList<String> targetClassNames) {
        super(parent);
        this.targetClassNames = targetClassNames;
    }

    public AVM_ClassLoader(ClassLoader parent, String targetClassName) {
        super(parent);
        targetClassNames = new ArrayList<>();
        targetClassNames.add(targetClassName);
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        boolean found = false;
        for (String s:targetClassNames){
            if (name.startsWith(s)){
                found = true;
                break;
            }
        }
        if (!found){            
            return super.loadClass(name);
        }
        try {
            URL url = getClass().getClassLoader().getResource(name.replace('.', '/') + ".class");
            URLConnection connection = url.openConnection();
            ByteArrayOutputStream buffer;
            try (InputStream input = connection.getInputStream()) {
                buffer = new ByteArrayOutputStream();
                int data = input.read();
                while (data != -1) {
                    buffer.write(data);
                    data = input.read();
                }
            }
            byte[] classData = buffer.toByteArray();
            Class loadedClass = defineClass(name, classData, 0, classData.length);
            return loadedClass;
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
