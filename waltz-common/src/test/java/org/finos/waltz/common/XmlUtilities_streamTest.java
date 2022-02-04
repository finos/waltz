package org.finos.waltz.common;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class XmlUtilities_streamTest {

    private static Document convertStringToXMLDocument(String xmlString)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Test
    public void simpleStream1(){
        String booksXml = "<bookstore>\n" +
                "    <book category=\"cooking\">\n" +
                "        <title lang=\"en\">Everyday Italian</title>\n" +
                "        <author>Giada De Laurentiis</author>\n" +
                "        <year>2005</year>\n" +
                "        <price>30.00</price>\n" +
                "    </book>\n" +
                "   <book category=\"cooking\">\n" +
                "       <title lang=\"en\">Everyday Italian</title>\n" +
                "       <author>Giada De Laurentiis</author>\n" +
                "       <year>2005</year>\n" +
                "       <price>30.00</price>\n" +
                "   </book></bookstore>";
        Document doc = convertStringToXMLDocument( booksXml );
        NodeList nl = doc.getElementsByTagName("book");
        Stream<Node> s = XmlUtilities.stream(nl);
        assertEquals(2, s.count());
    }

    @Test
    public void simpleStream2(){
        String booksXml = "<bookstore>\n" +
                "    <book category=\"cooking\">\n" +
                "        <title lang=\"en\">Everyday Italian</title>\n" +
                "        <author>Giada De Laurentiis</author>\n" +
                "        <year>2005</year>\n" +
                "        <price>30.00</price>\n" +
                "    </book>\n" +
                "   <book category=\"cooking\">\n" +
                "       <title lang=\"en\">Everyday Italian</title>\n" +
                "       <author>Giada De Laurentiis</author>\n" +
                "       <year>2005</year>\n" +
                "       <price>30.00</price>\n" +
                "   </book></bookstore>";
        Document doc = convertStringToXMLDocument( booksXml );
        NodeList nl = doc.getElementsByTagName("ele");
        Stream<Node> s = XmlUtilities.stream(nl);
        assertEquals(0, s.count());
    }
}
