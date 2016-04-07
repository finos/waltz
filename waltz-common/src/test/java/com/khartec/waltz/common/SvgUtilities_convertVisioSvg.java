package com.khartec.waltz.common;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;

public class SvgUtilities_convertVisioSvg {

    @Test
    public void foo() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {

        InputStream stream = SvgUtilities_convertVisioSvg.class
                .getClassLoader()
                .getResourceAsStream("t1-visio.svg");

        String svg = IOUtilities.readFully(stream);


        String result = SvgUtilities.convertVisioSvg("emp", svg);

        System.out.println(result);

    }
}
