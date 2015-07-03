package avl.sv.shared.solution.xml;

import avl.sv.shared.model.classifier.ClassifierInterface;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.solution.Solution;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
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

public class SolutionXML_Writer {
    
    public static String getXMLString(Solution solution) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
      
        try {
            // root elements
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = doc.createElement("Solution");
            Properties solutionProperties = solution.getProperties();
            for (String key:solutionProperties.stringPropertyNames()){
                rootElement.setAttribute(key, solutionProperties.getProperty(key));
            }
            doc.appendChild(rootElement);

            for (ClassifierInterface classifier : solution.getClassifiers()) {
                Element classifierElement = doc.createElement("Classifier");
                classifierElement.setAttribute("Class", classifier.getClass().getCanonicalName());
                classifierElement.appendChild(classifier.getProperties(doc));
                rootElement.appendChild(classifierElement);
            }

            for (AbstractFeatureGenerator featureGenerator:solution.getFeatureGenerators()){
                Element featuresNode = doc.createElement("FeatureGenerator");
                rootElement.appendChild(featuresNode);
                String[] featureNames = featureGenerator.getFeatureNames();
                StringBuilder sb = new StringBuilder();
                for (String featureName:featureNames){
                    sb.append(featureName).append(",");
                }
                featuresNode.setAttribute("generatorName",featureGenerator.getClass().getName());
                featuresNode.setAttribute("featureNames", sb.toString());
                featuresNode.setAttribute("isactive", String.valueOf(featureGenerator.isactive));
            }

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));


        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return bos.toString();
    }
    
    private static void createPropertiesNode(){
        
    }
    
}
