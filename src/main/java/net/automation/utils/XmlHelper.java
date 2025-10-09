package net.automation.utils;

import lombok.experimental.UtilityClass;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;

@UtilityClass
public class XmlHelper {
    public static  <T> T convertFromXml(Class<T> clazz, String xml) {
        T instance = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            instance = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            Assert.fail("Cannot convert xml into " + clazz.getName() + ". Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
        }

        return instance;
    }

    public static  <T> T convertFromXmlNode(Class<T> clazz, Node node) {
        T instance = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            instance = (T) unmarshaller.unmarshal(node, clazz).getValue();
        } catch (JAXBException e) {
            Assert.fail("Cannot convert xml into " + clazz.getName() + ". Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
        }

        return instance;
    }

    public static boolean checkXPath(String xmlString, String xpathExpression) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expression = xpath.compile(xpathExpression);
            Object result = expression.evaluate(document, XPathConstants.NODESET);
            return result != null && ((NodeList) result).getLength() > 0;
        } catch (Exception e) {
            Assert.fail("Cannot check xpath expression. XML: \n" + xmlString + "\nDetails: " + ExceptionHelper.getDetailedExceptionInfo(e));
        }

        return false;
    }

    public static List<String> getValues(String xml, String xpathNode) {
        List<String> outputValues = new ArrayList<>();

        try {
            Document doc = readDocument(xml);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(xpathNode, doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                outputValues.add(node.getTextContent().trim());
            }
        } catch (Exception ex) {
            fail("Cannot get values from XML. Details: %s".formatted(ex.getMessage()));
        }

        return outputValues;
    }

    public static String updateXmlNodes(String xml, Map<String, String> xpathNewValuesMap) {
        try {
            //xml = xml.replaceAll(">\\s+<", "><");
            Document doc = readDocument(xml);
            XPath xpath = XPathFactory.newInstance().newXPath();

            for (Map.Entry<String, String> entry : xpathNewValuesMap.entrySet()) {
                String nodeXPath = entry.getKey();
                String newValue = entry.getValue();
                Node node = (Node) xpath.evaluate(nodeXPath, doc, XPathConstants.NODE);
                node.setTextContent(newValue);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            xml = writer.getBuffer().toString();
        } catch (Exception ex) {
            fail("Cannot update XML. Details: %s".formatted(ex.getMessage()));
        }

        return xml;
    }

    public static void assertNodeValue(String xml, String nodeXPath, String expectedValue) {
        try {
            Document doc = readDocument(xml);
            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList nodeList = (NodeList) xpath.evaluate(nodeXPath, doc, XPathConstants.NODESET);
            if (nodeList.getLength() != 1) {
                fail("Xpath '%s' is not valid or node is not available in XML. ".formatted(nodeXPath));
            }
            Node node = nodeList.item(0);
            assertEquals("Invalid value for " + nodeXPath, node.getTextContent().trim(), expectedValue);

        } catch (Exception ex) {
            fail("Cannot find value in XML. Details: " + ex.getMessage());
        }
    }

    public static <T> String convertToXml(Class<T> clazz, T object) {
        String xml = null;

        try {
            StringWriter stringWriter = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, stringWriter);
            xml = stringWriter.toString();
        } catch (JAXBException e) {
            fail("Cannot convert object into XML. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
        }

        return xml;
    }

    public static String addAttributesToXmlRoot(String xmlString, Map<String, String> attributes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream is = new ByteArrayInputStream(xmlString.getBytes());
            Document document = builder.parse(is);
            Element root = document.getDocumentElement();

            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                if (!root.hasAttribute(attribute.getKey())) {
                    root.setAttribute(attribute.getKey(), attribute.getValue());
                }
            }

            return convertToXml(document);
        } catch (Exception e) {
            fail("Cannot add custom namespaces. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
            return null;
        }
    }

    private static String convertToXml(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private static Document readDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder  builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        return doc;
    }

}
