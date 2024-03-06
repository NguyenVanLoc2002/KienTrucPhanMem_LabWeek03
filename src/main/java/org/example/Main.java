package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        try (PrintWriter out = new PrintWriter(new FileOutputStream("results.xml"),true))
//        {
//
//            jdepend.xmlui.JDepend xml = new jdepend.xmlui.JDepend(out);
//            xml.addDirectory("T:\\teo\\Library-Assistant");
//
//            PackageFilter f = PackageFilter.all();
////            f.including("vn.edu.iuh");
//            f.accept("vn.edu");
//
//            f.excluding("org");
//            xml.setFilter(f);
//
//            xml.analyze();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        String xmlPath = "T:\\AllHK\\HK8\\KTPM\\BaiTap\\exercise03\\results.xml";
        String packagePrefix = "be.quodlibet.boxable";
        try {
            convertXMLToHTML(xmlPath, packagePrefix);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convertXMLToHTML(String xmlPath, String packagePrefix) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(xmlPath));
        document.getDocumentElement().normalize();

        NodeList packageNodes = document.getElementsByTagName("Package");
        List<JSONObject> allPackages = filterPackages(packageNodes, packagePrefix);

        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(allPackages);


        String json = jsonArray.toJSONString();

        String template = Files.readString(Paths.get("T:\\AllHK\\HK8\\KTPM\\BaiTap\\exercise03\\src\\main\\resources\\template\\index.html"));
        String result = template.replace("%%points_placeholder%%", json);
        Files.write(Paths.get("./index.html"), result.getBytes());
        System.out.println("Your report has been written to index.html. Open the file with a web browser to visualize.");
    }

    public static List<JSONObject> filterPackages(NodeList list, String packagePrefix) {
        List<JSONObject> filteredPackages = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String packageName = element.getAttribute("name");
                if (packageName.startsWith(packagePrefix)) {
                    JSONObject packageObject = new JSONObject();
                    JSONObject packageAttrs = new JSONObject();
                    packageAttrs.put("@_name", packageName);
                    packageObject.put("@_attrs", packageAttrs);

                    // Xử lý Stats
                    Element statsElement = (Element) element.getElementsByTagName("Stats").item(0);
                    JSONObject statsObject = new JSONObject();
                    NodeList statNodes = statsElement.getChildNodes();
                    for (int j = 0; j < statNodes.getLength(); j++) {
                        Node statNode = statNodes.item(j);
                        if (statNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element statElement = (Element) statNode;
                            String statName = statElement.getTagName();
                            String statValue = statElement.getTextContent();
                            statsObject.put(statName, Double.parseDouble(statValue));
                        }
                    }
                    packageObject.put("Stats", statsObject);

                    // Xử lý AbstractClasses
                    Element abstractClassesElement = (Element) element.getElementsByTagName("AbstractClasses").item(0);
                    if (abstractClassesElement != null && abstractClassesElement.hasChildNodes()) {
                        JSONArray abstractClassesArray = new JSONArray();
                        NodeList abstractClassNodes = abstractClassesElement.getElementsByTagName("Class");
                        for (int k = 0; k < abstractClassNodes.getLength(); k++) {
                            Node abstractClassNode = abstractClassNodes.item(k);
                            if (abstractClassNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element abstractClassElement = (Element) abstractClassNode;
                                JSONObject abstractClassObject = new JSONObject();
                                abstractClassObject.put("#text", abstractClassElement.getTextContent());
                                JSONObject classAttrs = new JSONObject();
                                classAttrs.put("@_sourceFile", abstractClassElement.getAttribute("sourceFile"));
                                abstractClassObject.put("@_attrs", classAttrs);
                                abstractClassesArray.add(abstractClassObject);
                            }
                        }
                        packageObject.put("AbstractClasses", abstractClassesArray);
                    }


                    // Xử lý ConcreteClasses
                    Element concreteClassesElement = (Element) element.getElementsByTagName("ConcreteClasses").item(0);
                    if (concreteClassesElement != null && concreteClassesElement.hasChildNodes()) {
                        JSONArray concreteClassesArray = new JSONArray();
                        NodeList concreteClassNodes = concreteClassesElement.getElementsByTagName("Class");
                        for (int k = 0; k < concreteClassNodes.getLength(); k++) {
                            Node concreteClassNode = concreteClassNodes.item(k);
                            if (concreteClassNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element concreteClassElement = (Element) concreteClassNode;
                                JSONObject concreteClassObject = new JSONObject();
                                concreteClassObject.put("#text", concreteClassElement.getTextContent());
                                JSONObject classAttrs = new JSONObject();
                                classAttrs.put("@_sourceFile", concreteClassElement.getAttribute("sourceFile"));
                                concreteClassObject.put("@_attrs", classAttrs);
                                concreteClassesArray.add(concreteClassObject);
                            }
                        }
                        packageObject.put("ConcreteClasses", concreteClassesArray);
                    } else {
                        packageObject.put("ConcreteClasses", "");
                    }

                    // Xử lý DependsUpon
                    Element dependsUponElement = (Element) element.getElementsByTagName("DependsUpon").item(0);
                    if (dependsUponElement != null && dependsUponElement.hasChildNodes()) {
                        JSONArray dependsUponArray = new JSONArray();
                        NodeList dependsUponNodes = dependsUponElement.getElementsByTagName("Package");
                        for (int l = 0; l < dependsUponNodes.getLength(); l++) {
                            Node dependsUponNode = dependsUponNodes.item(l);
                            if (dependsUponNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element dependsUponPackageElement = (Element) dependsUponNode;
                                dependsUponArray.add(dependsUponPackageElement.getTextContent());
                            }
                        }
                        JSONObject dependsUponObject = new JSONObject();
                        dependsUponObject.put("Package", dependsUponArray);
                        packageObject.put("DependsUpon", dependsUponObject);
                    } else {
                        packageObject.put("DependsUpon", "");
                    }

                    // Xử lý UsedBy
                    Element usedByElement = (Element) element.getElementsByTagName("UsedBy").item(0);
                    if (usedByElement != null && usedByElement.hasChildNodes()) {
                        JSONArray usedByArray = new JSONArray();
                        NodeList usedByNodes = usedByElement.getElementsByTagName("Package");
                        for (int m = 0; m < usedByNodes.getLength(); m++) {
                            Node usedByNode = usedByNodes.item(m);
                            if (usedByNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element usedByPackageElement = (Element) usedByNode;
                                usedByArray.add(usedByPackageElement.getTextContent());
                            }
                        }
                        JSONObject usedByObject = new JSONObject();
                        usedByObject.put("Package", usedByArray);
                        packageObject.put("UsedBy", usedByObject);
                    } else {
                        packageObject.put("UsedBy", "");
                    }

                    filteredPackages.add(packageObject);
                }
            }
        }
        return filteredPackages;
    }


}
