package org.finos.waltz.common;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlUtilities_printDocumentTest {

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
    public void simplePrintDocument1() throws TransformerException {
        String booksXml = "<bookstore><book category=\"cooking\"><title lang=\"en\">Everyday Italian</title><author>Giada De Laurentiis</author></book></bookstore>";
        Document doc = convertStringToXMLDocument(booksXml);
        String docStr = XmlUtilities.printDocument(doc,true);
        assertTrue(docStr.contains("\n"));
    }

    @Test
    public void simplePrintDocument2() throws TransformerException {
        String booksXml = "<bookstore><book category=\"cooking\"><title lang=\"en\">Everyday Italian</title><author>Giada De Laurentiis</author></book></bookstore>";
        Document doc = convertStringToXMLDocument(booksXml);
        String docStr = XmlUtilities.printDocument(doc,false);
        assertFalse(docStr.contains("\n"));
    }
}
