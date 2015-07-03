package avl.sv.shared.study.xml;

import avl.sv.shared.study.Attribute;
import avl.sv.shared.study.ROI;
import avl.sv.shared.study.ROIOval;
import avl.sv.shared.study.ROIPoly;
import avl.sv.shared.study.ROIRectangle;
import avl.sv.shared.NamedNodeMapFunc;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ROI_XML_Parser {
    
    public static ROI parse(String s) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance( );
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(s.getBytes());
        Document doc = docBuilder.parse(is);
        return ROI_XML_Parser.parse(doc);             
    }
    
    public static ROI parse(Document doc) throws ParserConfigurationException, SAXException, IOException {
        doc.getDocumentElement().normalize();
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Region".equalsIgnoreCase(n.getNodeName())) {
                return ROI_XML_Parser.parseRegion(n);
            }
        }
        return null;
    }
    
    protected static ArrayList<ROI> parseRegions(Node n) {
        ArrayList<ROI> rois = new ArrayList<>();
        for (n = n.getFirstChild(); n!=null;n = n.getNextSibling()){
            if ("Region".equalsIgnoreCase(n.getNodeName())) {
                rois.add(parseRegion(n));
            }
        }
        return rois;
    }
    
    private static ROI convertROI(ROI in, boolean fixedSize) {
        Polygon poly = in.getPolygon();
        ROI out = in;
        switch (poly.npoints){
            case 2:
                ROIOval oval = ROIOval.getDefault();
                int x[]= poly.xpoints;
                int y[]= poly.ypoints;
                if (x[0] > x[1]){
                    x = new int[]{x[1],x[0]};
                }
                if (y[0] > y[1]){
                    y = new int[]{y[1],y[0]};
                }
                oval.setOval((x[0]+x[1])/2, (y[0]+y[1])/2, (x[1]-x[0]), (y[1]-y[0]));
                if (fixedSize){
                    oval.setFixedSize(new int[]{(int)oval.oval.width, (int)oval.oval.height});
                }
                copyParams(in, oval);
                out = oval;
            case 4:
                Area area = new Area(poly);
                Rectangle rect = poly.getBounds();
                if (area.isRectangular()) {
                    ROIRectangle roi = ROIRectangle.getDefault();
                    roi.setRectangle(new Rectangle(rect.x, rect.y, rect.width, rect.height));
                    if (fixedSize) {
                        roi.setFixedSize(new int[]{(int) rect.width, (int) rect.height});
                    }
                    copyParams(in, roi);
                    out = roi;
                } else {
                    ROIOval roi = ROIOval.getDefault();
                    roi.setOval((int) rect.getCenterX(), (int) rect.getCenterY(), rect.width, rect.height);
                    if (fixedSize) {
                        roi.setFixedSize(new int[]{(int) rect.width, (int) rect.height});
                    }
                    copyParams(in, roi);
                    out = roi;
                }
        }
        out.setCreated();
        return out;
    }

    private static void copyParams(ROI from, ROI to) {
        to.setAnalyze(from.isAnalyze());
        to.attributes = from.attributes;
        to.selected = from.selected;
        to.displayId = from.displayId;
        to.highlighted = from.highlighted;
        to.id = from.id;
        to.negativeROA = from.negativeROA;
        to.selectedPoints = from.selectedPoints;
        to.isPosted = from.isPosted;
        to.setNameDirect(from.getName(), true);
    }
    
    private static ArrayList<Attribute> parseRegionAttributes(Node n) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("Attribute".equalsIgnoreCase(n.getNodeName())) {
                Attribute attr = new Attribute();
                attr.id = (NamedNodeMapFunc.getInteger(n.getAttributes(), "Id"));
                attr.name = (NamedNodeMapFunc.getString(n.getAttributes(), "Name"));
                attr.value = (NamedNodeMapFunc.getString(n.getAttributes(),"Value"));
                attributes.add(attr);
            }
        }
        return attributes;
    }    
    
    private static ROI parseRegion(Node n) {
        NamedNodeMap a = n.getAttributes();
        ROI roi = ROIPoly.getDefault();
        roi.setAnalyze(NamedNodeMapFunc.getBoolean(a,"Analyze"));
        roi.id = NamedNodeMapFunc.getLong(a, "Id");
        roi.negativeROA = NamedNodeMapFunc.getBoolean(a, "NegativeROA");
        roi.setNameDirect(NamedNodeMapFunc.getString(a, "Text"), true);
        roi.displayId = NamedNodeMapFunc.getInteger(a, "DisplayId");
        roi.selected = NamedNodeMapFunc.getBoolean(a, "Selected");
        roi.isPosted = NamedNodeMapFunc.getBoolean(a, "Posted");
        boolean fixedSize = NamedNodeMapFunc.getBoolean(a, "FixedSize");
        for (n = n.getFirstChild(); n!=null;n = n.getNextSibling()){
            if ("Attributes".equalsIgnoreCase(n.getNodeName())) {
                ArrayList<Attribute> attributes = parseRegionAttributes(n);
                for (Attribute attribute:attributes){
                    roi.attributes.add(attribute);
                }
            }
            if ("Vertices".equalsIgnoreCase(n.getNodeName())) {
                ArrayList<Point2D.Double> points = parseVertices(n);
                for (Point2D.Double point:points){
                    roi.addPoint(point.x, point.y);
                }
            }
        }
        roi.setCreated();
        return convertROI(roi, fixedSize);
    }
     
    private static ArrayList<Point2D.Double> parseVertices(Node n) {
        ArrayList<Point.Double> points = new ArrayList<>();
        for (n = n.getFirstChild(); n!=null;n = n.getNextSibling()){
            if ("Vertex".equalsIgnoreCase(n.getNodeName())) {
                double x = 0, y = 0;
                try {
                    x = NamedNodeMapFunc.getDouble(n.getAttributes(),"X");
                    y = NamedNodeMapFunc.getDouble(n.getAttributes(),"Y");
                } finally {
                    points.add(new Point.Double(x, y));
                }
            }
        }
        return points;
    }
}
