package linecounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LineCounter {

    
    public static void main(String[] args) {
        String base = "C:\\Ben\\virtualMicroscope\\netbeans\\";
        String sources[] = new String[]{
            base + "advanced-virtual-microscope\\AVM_Admin\\",
            base + "advanced-virtual-microscope\\AVM_Client\\",
            base + "advanced-virtual-microscope\\AVM_Client_Web\\",
            base + "advanced-virtual-microscope\\AVM_Lib\\",
            base + "advanced-virtual-microscope\\AVM_Server\\",
            base + "advanced-virtual-microscope\\TiffReader\\",
            base + "lib\\avl-intelligentscissors-gigapixel\\",
            base + "lib\\avl-jocl-features\\"};
        int count = 0;
        int subCount = 0;
        for (String sourceDir : sources) {
            for (File f : new File(sourceDir+"src\\").listFiles()) {
                subCount += countLines(f);
            }
            System.out.println(new File(sourceDir).getName() + ": " + String.valueOf(subCount));
            count += subCount;
            subCount = 0;
        }
        System.out.println("Total: " + String.valueOf(count));
    }
    
    private static int countLines(File file){
        int count = 0;
        if (file.isDirectory()){
            for (File sub:file.listFiles()) {
                count += countLines(sub);
            }
        } else {
            String name = file.getName();
            if (name.contains(".")) {
                String ext = name.substring(name.lastIndexOf("."));
                if (ext.equalsIgnoreCase(".java") || ext.equalsIgnoreCase(".cl")) {
                    System.out.println(name);
                    try {
                        String s = readFile(file);
                        String[] lines = s.split("\n");
                        for (String line:lines){
                            if (!line.startsWith("\\\\") && !line.startsWith("package") && !line.startsWith("import") && (line.length() > 2) ){
                                count++;
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(LineCounter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return count;
    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

}
