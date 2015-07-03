package avl.sv.client.fileFilters;

import java.io.File;
import javax.swing.filechooser.*;

public class XML_Filter extends FileFilter {

    private static String extentions[] = {".xml"};
    
    public static String getDefaultExention(){
        return extentions[0];
    }
    
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String test = f.getName();
        if (!test.contains(".")){
            return false;
        }
        String ext = test.substring(test.lastIndexOf(".")).toLowerCase();
        if (ext != null) {
            for (String e:extentions){
                if (ext.compareToIgnoreCase(e)==0 ) {
                        return true;
                }    
            }

        }
        return false;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("Aperio Imagescope Annotations:");
        for (String e:extentions){
            sb.append(" *").append(e);
        }
        return  sb.toString();
    }
    
    
}
