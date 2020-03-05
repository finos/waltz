package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.XmlUtilities
 *
 * @author Diffblue JCover
 */

public class XmlUtilitiesTest {

    @Test
    public void createNonValidatingDocumentBuilderFactory() throws javax.xml.parsers.ParserConfigurationException {
        DocumentBuilderFactory result = XmlUtilities.createNonValidatingDocumentBuilderFactory();
        assertThat(result.getSchema(), is(nullValue()));
        assertThat(result.isCoalescing(), is(false));
        assertThat(result.isExpandEntityReferences(), is(true));
        assertThat(result.isIgnoringComments(), is(false));
        assertThat(result.isIgnoringElementContentWhitespace(), is(false));
        assertThat(result.isNamespaceAware(), is(true));
        assertThat(result.isValidating(), is(false));
        assertThat(result.isXIncludeAware(), is(false));
    }

    @Test
    public void printDocument() throws javax.xml.parsers.ParserConfigurationException, javax.xml.transform.TransformerException {
        assertThat(XmlUtilities.printDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(), false), is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"));
        assertThat(XmlUtilities.printDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(), true), is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n\n"));
    }
}
