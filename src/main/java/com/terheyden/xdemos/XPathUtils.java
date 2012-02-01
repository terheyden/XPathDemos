package com.terheyden.xdemos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

public class XPathUtils {

    private static String exampleXML =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
        "\n" +
        "<bookstore>\n" +
        "\n" +
        "<book>\n" +
        "  <title lang=\"eng\">Harry Potter</title>\n" +
        "  <price>29.99</price>\n" +
        "<awesome/>" +
        "</book>\n" +
        "\n" +
        "<book>\n" +
        "  <title lang=\"eng\">Learning XML</title>\n" +
        "  <price>39.95</price>\n" +
        "<awesome stats=\"off\"/>" +
        "</book>\n" +
        "\n" +
        "</bookstore>";

    /**
     * A simple example upon which the utils are built.
     * Docs: http://www.saxonica.com/documentation/xpath-api/s9api-xpath.xml
     */
    private static void simple() {
        try {

            // XPath objs:
            Processor proc = new Processor(false);
            XPathCompiler xpath = proc.newXPathCompiler();
            DocumentBuilder builder = proc.newDocumentBuilder();

            // Load the XML document.
            // Note that builder.build() can also take a File arg.
            StringReader reader = new StringReader(exampleXML);
            XdmNode doc = builder.build(new StreamSource(reader));

            // Select all <book> nodes.
            // XPath syntax: http://www.w3schools.com/xpath/xpath_syntax.asp
            XPathSelector selector = xpath.compile("//book").load();
            selector.setContextItem(doc);

            // Evaluate the expression.
            XdmValue children = selector.evaluate();

            for (XdmItem item : children) {

                // Each book node has a title and price.
                // Get the title and show it.
                XdmNode bookNode = (XdmNode) item;
                System.out.println(getXdmNodeAsXML(bookNode, null));
                XdmNode titleNode = getChild(bookNode, "title");
                String title = titleNode.getStringValue();
                String lang = titleNode.getAttributeValue(new QName("lang"));
                System.out.println(String.format("%s (%s)", title, lang));
            }

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * Perform an XPath query search and return the results.
     * You can process the results in a number of ways.
     * @throws SaxonApiException
     */
    public static XdmValue xpathSearch(String xml, String xpathSearch) throws SaxonApiException {

        // XPath objs:
        Processor proc = new Processor(false);
        XPathCompiler xpath = proc.newXPathCompiler();

        // Do namespaces.
        Map<String, String> nsMap = parseNamespaces(xml);
//        xpath.declareNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

        for (String ns : nsMap.keySet()) {
            xpath.declareNamespace(ns, nsMap.get(ns));
        }

        DocumentBuilder builder = proc.newDocumentBuilder();
        builder.setDTDValidation(false);

        // Load the XML document.
        // Note that builder.build() can also take a File arg.
        StringReader reader = new StringReader(xml);
        XdmNode doc = builder.build(new StreamSource(reader));

        // Select all <book> nodes.
        // XPath syntax: http://www.w3schools.com/xpath/xpath_syntax.asp
        XPathSelector selector = xpath.compile(xpathSearch).load();
        selector.setContextItem(doc);

        // Evaluate the expression.
        return selector.evaluate();
    }

    /**
     * Helper method to get the first child of an element having a given name.
     * If there is no child with the given name it returns null.
     */
    public static XdmNode getChild(XdmNode parent, String childName) {

        XdmSequenceIterator iter = parent.axisIterator(Axis.CHILD, new QName(childName));

        if (iter.hasNext()) {
            return (XdmNode)iter.next();
        } else {
            return null;
        }
    }

    /**
     * Indent amount used by the "XML" methods when generating XML.
     * Defaults to 4 spaces.
     */
    public static String indentAmt = "    ";

    /**
     * Converts the {@link XdmValue} results of an XPath query to a list of string results.
     */
    public static List<String> getXdmValueAsList(XdmValue xdmValue) {

        List<String> list = new LinkedList<String>();

        for (XdmItem item : xdmValue) {
            XdmNode node = (XdmNode) item;
            list.add(getXdmNodeValue(node));
        }

        return list;
    }

    /**
     * Converts an {@link XdmNode} to a string value.
     */
    public static String getXdmNodeValue(XdmNode node) {

        if (node == null) {
            return "";
        }

        QName qname = node.getNodeName();
        String nodeName = qname == null ? null : qname.toString().trim();
        String nodeValue = node.getStringValue().trim();
        boolean hasNodeName = nodeName != null && !nodeName.isEmpty();
        boolean hasNodeValue = nodeValue != null && !nodeValue.isEmpty();

        if (hasNodeValue) {
            return nodeValue;
        }

        if (hasNodeName) {
            return nodeName;
        }

        return "";
    }

    /**
     * Walks XPath results and tries to create an accurate XML version from them.
     * @param xdmValue XPath results returned from {@link XPathUtils#xpathSearch(String, String)}.
     * @return XML representation of the XdmValue passed in
     */
    public static String getXdmValueAsXML(XdmValue xdmValue) {

        StringBuilder builder = new StringBuilder();

        for (XdmItem item : xdmValue) {

            XdmNode node = (XdmNode) item;
            builder.append(XPathUtils.getXdmNodeAsXML(node, null));
        }

        return builder.toString();
    }

    /**
     * Walks an XML node and creates it as a huge string.
     * Used by the XML UI tester.
     * @param node node returned by a XPath query
     * @param indent amount of indent to put before each lined - used recursively - just pass null
     * @return string ready to output to user
     */
    public static String getXdmNodeAsXML(XdmNode node, String indent) {

        if (node == null) {
            return "";
        }

        QName qname = node.getNodeName();
        String nodeName = qname == null ? null : qname.toString().trim();
        String nodeValue = node.getStringValue().trim();
        boolean hasNodeName = nodeName != null && !nodeName.isEmpty();
        boolean hasNodeValue = nodeValue != null && !nodeValue.isEmpty();

        // If both the node name and value are blank, this is
        // most likely a whitespace filler node; ignore it.
        if (!hasNodeName && !hasNodeValue) {
            return "";
        }

        // If no node name, but there is a value, this is
        // most likely a leaf value.
        if (!hasNodeName) {
            return String.format("%s%s\n", indent, nodeValue);
        }

        if (indent == null) {
            indent = "";
        }

        StringBuilder builder = new StringBuilder();
        StringBuilder attrBuilder = new StringBuilder();

        // Get the node's attributes. E.g. <node attr1="value1" attr2="value2">
        XdmSequenceIterator attrIter = node.axisIterator(Axis.ATTRIBUTE);
        boolean hasAttributes = attrIter.hasNext();

        while (attrIter.hasNext()) {
            XdmNode nextAttr = (XdmNode) attrIter.next();
            attrBuilder.append(String.format(" %s=\"%s\"", nextAttr.getNodeName(), nextAttr.getStringValue()));
        }

        XdmSequenceIterator childIter = node.axisIterator(Axis.CHILD);
        boolean hasChildren = childIter.hasNext();

        // If the node has a name, but no value and no children,
        // it is most likely a one-line <node/>
        if (!hasChildren && !hasNodeValue) {
            return String.format("%s<%s%s/>\n", indent, nodeName, attrBuilder.toString());
        }

        // If the node has a name / value, no attributes and no children,
        // it's probably an attribute (e.g. the user searched for: //@attrName).
        if (!hasChildren && !hasAttributes && hasNodeName && hasNodeValue) {
            return String.format("%s%s=\"%s\"\n", indent, nodeName, nodeValue);
        }

        // Write the opening <node>
        builder.append(String.format("%s<%s%s>\n", indent, nodeName, attrBuilder.toString()));

        // Write the children.
        while (childIter.hasNext()) {
            XdmNode child = (XdmNode) childIter.next();
            builder.append(getXdmNodeAsXML(child, indent + indentAmt));
        }

        // Write the closing </node>
        builder.append(String.format("%s</%s>\n", indent, node.getNodeName()));

        return builder.toString();
    }

    /**
     * Extract namespaces into a map for adding to the builder.
     * E.g. xmlns:xsd="http://www.w3.org/2001/XMLSchema"
     * @param xml XML source
     * @return map of namespaces : URL, e.g. [ "xsd" : "http://www.w3.org/2001/XMLSchema" ]
     */
    private static Map<String, String> parseNamespaces(String xml) {

        Map<String, String> nsMap = new HashMap<String, String>();

        Pattern nsPat = Pattern.compile("xmlns:(\\w+)=\"([^\"]+)\"");
        Matcher nsMat = nsPat.matcher(xml);

        while (nsMat.find()) {
            String ns = nsMat.group(1);
            String url = nsMat.group(2);
            nsMap.put(ns, url);
        }

        return nsMap;
    }

    //////////////////////////////////////////////////////
    // Not really XPath related but useful for scripting:

    /**
     * Reads a file relative to the dir this app was started from.
     * @param filename relative filename to load
     * @return entire file as a String
     * @throws FileNotFoundException if file not found!
     */
    public static String readFile(String filename) throws FileNotFoundException {

        String startDir = System.getProperty("user.dir");
        File propertyFile = new File(startDir, filename);

        Scanner scan = new Scanner(new FileInputStream(propertyFile));
        scan.useDelimiter("\\Z");
        String content = scan.next();
        scan.close();

        return content;
    }

}
