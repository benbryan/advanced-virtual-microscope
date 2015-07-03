package avl.sv.shared.solution.xml;

import avl.sv.shared.NamedNodeMapFunc;
import avl.sv.shared.model.classifier.ClassifierInterface;
import avl.sv.shared.model.featureGenerator.AbstractFeatureGenerator;
import avl.sv.shared.solution.Solution;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SolutionXML_Parser {

    public Solution parse(String s) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance( );
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(s.getBytes());
        Document doc = docBuilder.parse(is);
        return parse(doc);
    }

    private Solution parse(Document doc) throws ParserConfigurationException, SAXException, IOException {
        doc.getDocumentElement().normalize();
        Solution solution = parsedoc(doc);
        return solution;
    }

    private Solution parsedoc(Node n) {
        Solution solution = null;
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Solution".equalsIgnoreCase(n.getNodeName())) {
                NamedNodeMap a = n.getAttributes();
                String solutionName = NamedNodeMapFunc.getString(a, "Name");
                solution = new Solution(solutionName);
                for (int i = 0; i < a.getLength(); i++){
                    Node node = a.item(i);
                    solution.setProperty(node.getNodeName(), node.getNodeValue());
                }
                ArrayList<ClassifierInterface> classifiers = parseClassifiers(n);
                for (ClassifierInterface classifier:classifiers){
                    solution.setClassifier(classifier);
                }
                ArrayList<AbstractFeatureGenerator> featureGenerators = parseFeatureGenerators(n);
                for (AbstractFeatureGenerator featureGenerator:featureGenerators){
                    solution.setFeatureGenerator(featureGenerator);
                }
                break;
            }
        }
        return solution;
    }

    private ArrayList<ClassifierInterface> parseClassifiers(Node n) {
        ArrayList<ClassifierInterface> classifiers = new ArrayList<>();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Classifier".equalsIgnoreCase(n.getNodeName())) {
                ClassifierInterface classifier = parseClassifier(n);
                if (classifier != null){
                    classifiers.add(classifier);
                }
            }
        }
        return classifiers;
    }
    
    private ArrayList<AbstractFeatureGenerator> parseFeatureGenerators(Node n) {
        ArrayList<AbstractFeatureGenerator> featureGenerators = new ArrayList<>();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("FeatureGenerator".equalsIgnoreCase(n.getNodeName())) {
                AbstractFeatureGenerator featureGenerator = parseFeatureGenerator(n);
                if (featureGenerator == null){
                    continue;
                }
                featureGenerators.add(featureGenerator);
            }
        }
        return featureGenerators;
    }
    
    private ClassifierInterface parseClassifier(Node n) {
        try {
            NamedNodeMap a = n.getAttributes();
            String classifierName = NamedNodeMapFunc.getString(a, "Class");
            Class<?> c = Class.forName(classifierName);
            Constructor constructor = c.getConstructor(new Class<?>[]{});
            ClassifierInterface classifier = (ClassifierInterface) constructor.newInstance(new Object[]{});
            classifier.setProperties(n);
            return classifier;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException ex) {
            System.out.println("Failed to recognize and parse a classifier");
        }
        return null;
    }

    private AbstractFeatureGenerator parseFeatureGenerator(Node n) {
        try {
            NamedNodeMap a = n.getAttributes();
            String generatorName = NamedNodeMapFunc.getString(a, "generatorName");
            Class<?> c = Class.forName(generatorName);
            Constructor constructor = c.getConstructor(new Class<?>[]{});
            AbstractFeatureGenerator featureGenerator = (AbstractFeatureGenerator) constructor.newInstance(new Object[]{});
            String featureNames = NamedNodeMapFunc.getString(a, "featureNames");
            boolean active = NamedNodeMapFunc.getBoolean(a, "isactive");
            featureGenerator.isactive = active;
            featureGenerator.setFeatureNames(featureNames.split(","));
            return featureGenerator;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException ex) {
            System.out.println("Failed to recognize and parse a feature generator");
        }
        return null;
    }
}


















