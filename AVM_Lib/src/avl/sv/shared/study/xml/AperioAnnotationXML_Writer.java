package avl.sv.shared.study.xml;

import avl.sv.shared.solution.xml.SolutionXML_Writer;
import avl.sv.shared.study.ROI_Folder;
import avl.sv.shared.study.ROI;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AperioAnnotationXML_Writer {

    public static String getXMLString(ArrayList<ROI_Folder> annos) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();

            Element annotationsNode = doc.createElement("Annotations");
            annotationsNode.setAttribute("MicronsPerPixel", "1");
            doc.appendChild(annotationsNode);

            AperioAnnotationXML_Writer.appendAnnotations(doc, annos, annotationsNode);

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bos));

        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(SolutionXML_Writer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bos.toString();
    }

    protected static void appendAnnotations(Document doc, ArrayList<ROI_Folder> annos, Element annotationsNode) {
        for (ROI_Folder anno : annos) {
            Element annotationNode = createAnnotationElement(doc, anno);
            annotationsNode.appendChild(annotationNode);
        }
    }

    protected static Element createAnnotationElement(Document doc, ROI_Folder anno) {
        Element annotationNode = doc.createElement("Annotation");

        String visible = (anno.visible) ? "1" : "0";

        annotationNode.setAttribute("Id", String.valueOf(anno.id));
        annotationNode.setAttribute("Name", anno.getName());
        annotationNode.setAttribute("LineColor", String.valueOf((long) Math.abs(anno.getLineColor().getRGB())));
        annotationNode.setAttribute("Visible", visible);

        annotationNode.setAttribute("ReadOnly", "0");
        annotationNode.setAttribute("NameReadOnly", "0");
        annotationNode.setAttribute("LineColorReadOnly", "0");
        annotationNode.setAttribute("Incremental", "0");
        annotationNode.setAttribute("MarkupImagePath", "");
        annotationNode.setAttribute("MacroName", "");

        ROI_Folder_XML_Writer.appendAnnotationAttributes(doc, anno, annotationNode);

        appendRegions(doc, anno.getROIs(), annotationNode);
        appendPlots(doc, anno.getROIs(), annotationNode);
        return annotationNode;
    }

    protected static void appendRegionAttributeHeaders(Document doc, Element annotationNode) {
        Element regionsNode = doc.createElement("RegionAttributeHeaders");
        annotationNode.appendChild(regionsNode);
        Element attributeHeaderNode;

        attributeHeaderNode = doc.createElement("AttributeHeader");
        attributeHeaderNode.setAttribute("Id", "9999");
        attributeHeaderNode.setAttribute("Name", "Region");
        attributeHeaderNode.setAttribute("ColumnWidth", "-1");
        regionsNode.appendChild(attributeHeaderNode);

        attributeHeaderNode = doc.createElement("AttributeHeader");
        attributeHeaderNode.setAttribute("Id", "9997");
        attributeHeaderNode.setAttribute("Name", "Length");
        attributeHeaderNode.setAttribute("ColumnWidth", "-1");
        regionsNode.appendChild(attributeHeaderNode);

        attributeHeaderNode = doc.createElement("AttributeHeader");
        attributeHeaderNode.setAttribute("Id", "9996");
        attributeHeaderNode.setAttribute("Name", "Area");
        attributeHeaderNode.setAttribute("ColumnWidth", "-1");
        regionsNode.appendChild(attributeHeaderNode);

        attributeHeaderNode = doc.createElement("AttributeHeader");
        attributeHeaderNode.setAttribute("Id", "9998");
        attributeHeaderNode.setAttribute("Name", "Text");
        attributeHeaderNode.setAttribute("ColumnWidth", "-1");
        regionsNode.appendChild(attributeHeaderNode);

        attributeHeaderNode = doc.createElement("AttributeHeader");
        attributeHeaderNode.setAttribute("Id", "1");
        attributeHeaderNode.setAttribute("Name", "Description");
        attributeHeaderNode.setAttribute("ColumnWidth", "-1");
        regionsNode.appendChild(attributeHeaderNode);
    }

    protected static void appendRegions(Document doc, ArrayList<ROI> rois, Element annotationNode) {
        Element regionsNode = doc.createElement("Regions");
        annotationNode.appendChild(regionsNode);
        appendRegionAttributeHeaders(doc, regionsNode);
        for (ROI r : rois) {
            appendRegion(doc, r, regionsNode);
        }
    }
    protected static void appendRegion(Document doc, ROI r, Element regionsNode) {
            String selected = (r.selected) ? "1" : "0";
            String negativeROA = (r.negativeROA) ? "1" : "0";
            String analyze = (r.isAnalyze()) ? "1" : "0";
            String type = String.valueOf(r.getType());

            Element regionNode = doc.createElement("Region");
            regionsNode.appendChild(regionNode);

            regionNode.setAttribute("Id", String.valueOf(r.id));
            regionNode.setAttribute("Type", type);
            regionNode.setAttribute("Selected", selected);
            regionNode.setAttribute("Text", r.getName());
            regionNode.setAttribute("NegativeROA", negativeROA);
            regionNode.setAttribute("Analyze", analyze);

            regionNode.setAttribute("Zoom", "0.015650");
            regionNode.setAttribute("ImageLocation", "");
            regionNode.setAttribute("ImageFocus", "0");
            regionNode.setAttribute("Length", "0");
            regionNode.setAttribute("Area", "0");
            regionNode.setAttribute("LengthMicrons", "0");
            regionNode.setAttribute("AreaMicrons", "0");
            regionNode.setAttribute("InputRegionId", "0");
            regionNode.setAttribute("DisplayId", "1");

            ROI_XML_Writer.appendRegionAttributes(doc, r, regionNode);
            ROI_XML_Writer.appendVertices(doc, r.getPolygon(), regionNode);
    }
    protected static void appendPlots(Document doc, ArrayList<ROI> rois, Element annotationNode) {
        Element plotsNode = doc.createElement("Plots");
        annotationNode.appendChild(plotsNode);
    }

}
