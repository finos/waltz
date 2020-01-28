/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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
import java.util.Optional;
import java.util.function.Function;

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


    public static String addWaltzEntityLinks(String svgStr,
                                             String keyProp,
                                             Function<String, Optional<String>> keyToUrl) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
        DocumentBuilder builder = createNonValidatingDocumentBuilderFactory().newDocumentBuilder();
        InputSource svgSource = new InputSource(new ByteArrayInputStream(svgStr.getBytes()));
        Document svg = builder.parse(svgSource);

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//*", svg, XPathConstants.NODESET);

        stream(nodes)
                .forEach(n -> stream(n.getChildNodes())
                        .filter(c -> c.hasAttributes() && c.getAttributes().getNamedItem("data-" + keyProp) != null)
                        .forEach(c -> {
                            String keyVal = c.getAttributes()
                                                .getNamedItem("data-" + keyProp)
                                                .getNodeValue();

                            keyToUrl.apply(keyVal)
                                    .ifPresent(url -> {
                                        Element linkNode = svg.createElement("a");
                                        linkNode.setAttribute("href", url);
                                        linkNode.setAttribute("target", "_blank");
                                        c.getParentNode().appendChild(linkNode);
                                        linkNode.appendChild(c);
                                    });
                        })
                );

        return printDocument(svg, false);
    }

}
