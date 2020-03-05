package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Test;

public class XmlUtilitiesDiffblueTest {
  @Test
  public void createNonValidatingDocumentBuilderFactoryTest() throws ParserConfigurationException {
    // Arrange, Act and Assert
    assertTrue(XmlUtilities
        .createNonValidatingDocumentBuilderFactory() instanceof com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl);
  }

  @Test
  public void printDocumentTest() throws TransformerException {
    // Arrange, Act and Assert
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + "\n" + "\n",
        XmlUtilities.printDocument(new CoreDocumentImpl(), true));
  }
}

