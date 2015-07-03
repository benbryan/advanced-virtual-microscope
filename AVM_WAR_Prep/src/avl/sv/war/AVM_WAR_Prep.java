package avl.sv.war;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class AVM_WAR_Prep {
    
    final private static String PROPERTIES_FILE_NAME = "war.properties";
    final public static String KEY_KEYSTORE_PATH = "keystorePath";
    final public static String KEY_CODEBASE = "codebase";
    final public static String KEY_JNLP_FILE_NAME = "jnlpFileName";
    final public static String KEY_CLIENT_JAR_NAME = "clientJarName";
    final public static String KEY_WAR_PATH = "warName";
    final public static String KEY_TSA = "tsa";
    final public static String KEY_STOREPASS = "storepass";
    final public static String KEY_ALIAS = "keyAlias";
    final public static String KEY_ARGUMENTS = "args";
    final public static String KEY_JDK_Path = "jdk_path";
    
    final private Properties properties;
    final private PrintStream printStream;

    public AVM_WAR_Prep(Properties properties, PrintStream printStream) {
        this.properties = properties;
        this.printStream = printStream;
    }   
    public AVM_WAR_Prep(Properties properties) {
        this.properties = properties;
        this.printStream = System.out;
    }   

    public static Properties getProperties() {
        Properties p = getDefaultProperties();
        try {
            p.loadFromXML(new FileInputStream(new File(PROPERTIES_FILE_NAME)));
        } catch (IOException ex) {}
        return p;
    }
    private static Properties getDefaultProperties(){
        Properties p = new Properties();
        p.setProperty(KEY_KEYSTORE_PATH, "KEYSTORE");
        p.setProperty(KEY_CODEBASE, "http://www.benbryan.info/AVM_Client_Web/");
        p.setProperty(KEY_JNLP_FILE_NAME, "AdvancedVirtualMicroscope.jnlp");
        p.setProperty(KEY_CLIENT_JAR_NAME, "AVM_Client.jar");
        p.setProperty(KEY_WAR_PATH, "AVM_Client_Web.war");
        p.setProperty(KEY_TSA, "http://timestamp.digicert.com");
        p.setProperty(KEY_STOREPASS, ""); 
        p.setProperty(KEY_ALIAS, ""); 
        p.setProperty(KEY_ARGUMENTS, "-server http://www.benbryan.info/AVM_Client_Web/");
        p.setProperty(KEY_JDK_Path, "C:/Program Files/Java/jdk1.8.0_31");
        return p;
    }
    public static void setProperty(String key, String value){
        Properties p = getProperties();
        p.put(key, value);
        try {
            File f = new File(PROPERTIES_FILE_NAME);
            if (!f.exists()){
                f.createNewFile();
            }
            p.storeToXML(new FileOutputStream(f), "");
        } catch (IOException ex) {
            Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public String signAVM(){
        File warFileIn = new File(properties.getProperty(KEY_WAR_PATH));
        File warFileBackup = new File(warFileIn + ".backup");
        final File warFileOut = warFileIn;
        if (!warFileIn.exists()){
            warFileIn = warFileBackup;
            if (!warFileIn.exists()){
                String errorMsg = "Cannot locate input war file";
                printStream.println(errorMsg);
                return errorMsg;
            }
            printStream.println("Backup war found, using it");
        } else {
            warFileIn.renameTo(warFileBackup);
            warFileIn = warFileBackup;
        }
        if (!warFileIn.exists()){
            String errorMsg = "Can not find input war file";
            printStream.println(errorMsg);
            return errorMsg;
        }
        final File workingDir = warFileIn.getParentFile();
        if (!workingDir.canWrite()){
        printStream.println("Cannot write to war location");
            return "Cannot write to war location";
        }    
        final File clientJarFile = new File(workingDir + File.separator + properties.getProperty(KEY_CLIENT_JAR_NAME));
        final File libDir = new File(workingDir + File.separator + "lib" + File.separator);
        
        // Extract client jar and libs to disk
        printStream.println("Loading war to memory");
        HashMap<String, byte[]> warFiles = readJAR(warFileIn);
        if (warFiles == null){
            String errorMsg = "Can not read input war file";
            printStream.println(errorMsg);
            return errorMsg;
        }

        try {  
            printStream.println("Extracting client jar " + properties.getProperty(KEY_CLIENT_JAR_NAME));
            byte clientJarData[] = warFiles.get(properties.getProperty(KEY_CLIENT_JAR_NAME));          
            try (FileOutputStream fos = new FileOutputStream(clientJarFile)) {
                fos.write(clientJarData);
            }
            
            libDir.mkdir();
            libDir.deleteOnExit();
            for (Map.Entry<String, byte[]> entry : warFiles.entrySet()) {
                String fileName = entry.getKey();
                if (fileName.startsWith("lib/")) {
                    printStream.println("    Extracting " + fileName);  
                    File f = new File(workingDir+File.separator+fileName);
                    if (f.isDirectory()){
                        continue;
                    }
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        fos.write(entry.getValue());
                    }
                    f.deleteOnExit();
                }
            }
            System.gc();
        } catch (IOException ex) {
            String errorMsg = "Failed to extract client jar titled: " + properties.getProperty(KEY_CLIENT_JAR_NAME);
            printStream.println(errorMsg);
            return errorMsg;
        }            
        printStream.println("Extracting jnlp");
        byte[] jnlpDataOriginal = warFiles.get(properties.getProperty(KEY_JNLP_FILE_NAME));
        
        printStream.println("Modifying jnlp");
        String jnlpFileNameX86 = "AVM_X86.jnlp";
        String jnlpFileNameX64 = "AVM_X64.jnlp";            
        byte[] jnlpDataX86 = modifyJNLP(jnlpDataOriginal, "-Xmx1g", jnlpFileNameX86);
        byte[] jnlpDataX64 = modifyJNLP(jnlpDataOriginal, "-Xmx64g", jnlpFileNameX64);

        printStream.println("Loading client jar to memory: from disk using JarFile class");
        HashMap<String, byte[]> clientFiles = readJAR(clientJarFile);
        if (clientFiles == null){
            String errorMsg = "Failed to read extracted client jar";
            printStream.println(errorMsg);
            return errorMsg;
        }
        
        printStream.println("Deleting client jar from disk");
        clientJarFile.delete();
        System.gc();
        
        printStream.println("Embeding jnlp in client jar");
        clientFiles.put("JNLP-INF/", new byte[0]);
        clientFiles.put("JNLP-INF/" + jnlpFileNameX86, jnlpDataX86);
        clientFiles.put("JNLP-INF/" + jnlpFileNameX64, jnlpDataX64);
        
        printStream.println("Saving client jar with modified jnlp to disk");        
        createJar(clientFiles, clientJarFile);
        
        // run gc a bunch to release file locks before signing
        for (int i = 0; i < 10; i++){
            Runtime.getRuntime().gc();
        }

        if (signJAR(clientJarFile)!= 0){
            String errorMsg = "Failed to sign client jar file";
            printStream.println(errorMsg);
            return errorMsg;
        }
        
        printStream.println("Extracting signed JNLPs");
        jnlpDataX86 = readJAR(clientJarFile).get("JNLP-INF/" + jnlpFileNameX86);
        jnlpDataX64 = readJAR(clientJarFile).get("JNLP-INF/" + jnlpFileNameX64);
        
        printStream.println("Replacing jnlp in war with signed version");
        warFiles.put(jnlpFileNameX86, jnlpDataX86);
        warFiles.put(jnlpFileNameX64, jnlpDataX64);
        
        if (signFolderOfJars(libDir)!= 0){
            String errorMsg = "Failed to sign libs in war";
            printStream.println(errorMsg);
            return errorMsg;
        }

        printStream.println("Reading and adding signed libs to war");
        for (File file:libDir.listFiles()){
            byte data[] = getFileAsByteArray(file);
            String entryName = file.getAbsolutePath().replace(workingDir+File.separator, "").replace(File.separator, "/");
            warFiles.replace(entryName, data);
        }
        
        printStream.println("Reading and adding signed client jar to war");
        warFiles.replace(properties.getProperty(KEY_CLIENT_JAR_NAME), getFileAsByteArray(clientJarFile));
        if (warFileOut.exists()){
            warFileOut.delete();
        }
        clientJarFile.deleteOnExit();
        
        printStream.println("Writing new war to disk");
        createJar(warFiles, warFileOut);
        System.gc();
        return "Processes Finished";
    }
        
    private HashMap<String, byte[]> readJAR(File clientJarFile) {
        HashMap<String, byte[]> files = new HashMap<>();
        try {
            JarFile jar = new JarFile(clientJarFile);
            Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) enumEntries.nextElement();
                ByteArrayOutputStream baos;
                try (BufferedInputStream bis = new BufferedInputStream(jar.getInputStream(entry))) {
                    baos = new ByteArrayOutputStream();
                    try (BufferedOutputStream bos = new BufferedOutputStream(baos)) {
                        while (bis.available() > 0) {
                            bos.write(bis.read());
                        }   bos.flush();
                    }
                    baos.flush();
                    baos.close();
                }
                files.put(entry.getName(), baos.toByteArray());
            }
        } catch (IOException ex) {
            return null;
        }
        return files;
    }

    private void createJar(HashMap<String, byte[]> files, File clientJarFile ) {
        JarOutputStream outputJar = null;
        byte oldManifest[] = files.remove("META-INF/MANIFEST.MF");
        try {
            Manifest manifest = new Manifest(new ByteArrayInputStream(oldManifest));            
            manifest.getMainAttributes().putValue("Codebase", properties.getProperty(KEY_CODEBASE));
            manifest.getMainAttributes().putValue("Permissions", "all-permissions");
            outputJar = new JarOutputStream(new FileOutputStream(clientJarFile), manifest);
            outputJar.setLevel(0);
            for (Map.Entry<String, byte[]> file:files.entrySet()){
                String fileName = file.getKey();
                byte data[] = file.getValue();
                JarEntry entry = new JarEntry(fileName);
                entry.setTime(new Date().getTime());
                outputJar.putNextEntry(entry);
                outputJar.write(data);
                outputJar.closeEntry();
            }
            outputJar.flush();
            outputJar.finish();
        } catch (IOException ex) {
            if (outputJar != null){
                try {
                    outputJar.close();
                } catch (IOException ex1) {
                    Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
    private int signJAR(File jarFile) {        
        printStream.println("    Signing jar " + jarFile.getName());
        String javaHome = properties.getProperty(KEY_JDK_Path) + File.separator + "bin" + File.separator;
        try {            
            String cmdSign = "\"" + javaHome + 
                    "jarsigner\" -tsa \"" + properties.getProperty(KEY_TSA) + "\""+
                    " -keystore \"" + properties.getProperty(KEY_KEYSTORE_PATH) + "\"" +
                    " -storepass " + properties.getProperty(KEY_STOREPASS) + 
                    " \"" + jarFile.getAbsolutePath() + "\" " + 
                    properties.getProperty(KEY_ALIAS);
            printStream.println("     Running process:" + cmdSign);
            Process processSign = Runtime.getRuntime().exec(cmdSign);
            String result = getResult(processSign);
            if (!result.contains("jar signed")){
                printStream.println("Failed to sign " + jarFile.getName());
                printStream.println(result);
                return -1;
            }
        } catch (IOException ex) {
            ex.printStackTrace(printStream);
            return -2;
        }
        return 0;
    }
    
    private String getResult(Process process) throws IOException{
        int count = 0;
        StringBuilder sb = new StringBuilder();
        while (process.isAlive() || (count = process.getInputStream().available())>0){
            if (count>0){
                byte b[] = new byte[count];
                process.getInputStream().read(b);
                sb.append(new String(b));
            }
        }
        return sb.toString();
    }

    private int signFolderOfJars(File dir) {
        for (File file:dir.listFiles()){
            if (file.getName().endsWith(".jar") || file.getName().endsWith(".JAR")){
                if (signJAR(file) != 0){
                    return -1;
                }
            }
        }      
        return 0;
    }

    private byte[] getFileAsByteArray(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int count;
            byte b[] = new byte[1024];
            while ((count = fis.read(b))>0){
                bos.write(b,0, count);
            }
            byte[] jnlpData = bos.toByteArray();
            return jnlpData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fis != null){
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private byte[] modifyJNLP(byte[] jnlpData, String xmx, String jnlpName) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(jnlpData));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Node jnlpNode = doc.getFirstChild();
            Element securityElement = doc.createElement("security");
            securityElement.appendChild(doc.createElement("all-permissions"));
            jnlpNode.appendChild(securityElement);
            jnlpNode.getAttributes().getNamedItem("codebase").setNodeValue(properties.getProperty(KEY_CODEBASE));
            jnlpNode.getAttributes().getNamedItem("href").setNodeValue(jnlpName);
            
            for (Node n = jnlpNode.getFirstChild(); (n = n.getNextSibling()) != null; ){
                if (n.getNodeName().equals("information")){
                    for (Node n1 = n.getFirstChild(); (n1 = n1.getNextSibling()) != null; ){
                        if (n1.getNodeName().equals("title")){
                            if (jnlpName.contains("X64")){
                                n1.setTextContent("Advanced Virtual Microscope 64-Bit");
                            } else if (jnlpName.contains("X86")){
                                n1.setTextContent("Advanced Virtual Microscope 32-Bit");
                            }
                        }
                    }
                }
                if (n.getNodeName().equals("resources")){
                    for (Node n1 = n.getFirstChild(); (n1 = n1.getNextSibling()) != null; ){
                        if (n1.getNodeName().equals("j2se")){
                            Element e = (Element) n1;
                            String args[] = e.getAttribute("java-vm-args").split(" ");
                            StringBuilder sb = new StringBuilder();
                            boolean heapAdded = false;
                            for (String arg:args){
                                if (arg.startsWith("-Xmx")){
                                    heapAdded = true;
                                    sb.append(xmx);
                                } else {
                                    sb.append(arg);
                                }
                                sb.append(" ");
                            }
                            if (!heapAdded){
                                sb.append(xmx).append(" ");
                            }
                            if (jnlpName.contains("X64")) {
                                sb.append("-d64 ");
                            } else if (jnlpName.contains("X86")) {
                                sb.append("-d32 ");
                            }
                            e.setAttribute("java-vm-args", sb.toString());
                        }
                    }
                }
                if (n.getNodeName().equals("application-desc")){
                    // Remove existing arguments
                    Node appNode = n;
                    ArrayList<Node> toRemove = new ArrayList<>();
                    for (Node n1 = appNode.getFirstChild(); (n1 = n1.getNextSibling()) != null; ){
                        if (n1.getNodeName().equals("argument")){
                            toRemove.add(n1);
                        }
                    }
                    for (Node node:toRemove){
                        appNode.removeChild(node);
                    }
                    // Add new arguments
                    for (String arg:properties.getProperty(KEY_ARGUMENTS).split(" ")){
                        Element element = doc.createElement("argument");
                        element.setTextContent(arg.trim());
                        appNode.appendChild(element);
                    }
                }
            }
            
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));
            jnlpData = bos.toByteArray();
            return jnlpData;
        } catch (TransformerException | ParserConfigurationException | SAXException | IOException ex) {
            printStream.println("Failed to modify jnlp");
            Logger.getLogger(AVM_WAR_Prep.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }
}
