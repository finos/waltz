/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.khartec.waltz.common.XmlUtilities.*;

public class SvgUtilities {

    public static String convertVisioSvg(String key, String svgStr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
        DocumentBuilder builder = createNonValidatingDocumentBuilderFactory().newDocumentBuilder();
        InputSource svgSource = new InputSource(new ByteArrayInputStream(svgStr.getBytes()));
        Document svg = builder.parse(svgSource);

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//*", svg, XPathConstants.NODESET);

        stream(nodes)
                .forEach(n -> stream(n.getChildNodes())
                        .filter(c -> c.getNodeName().contains("custProps"))
                        .forEach(c -> stream(c.getChildNodes())
                                .filter(cp -> cp.getNodeName().contains("cp"))
                                .map(cp -> (Element) cp)
                                .filter(cp -> key.equals(cp.getAttribute("v:lbl")))
                                .map(cp -> cp.getAttribute("v:val"))
                                .map(v -> v.replaceAll("^.*\\((.*)\\)$", "$1"))
                                .forEach(v -> ((Element) n).setAttribute("data-"+key, v))
                        )
                );

        return printDocument(svg, false); // do NOT toPrettyString print visio
    }

}
