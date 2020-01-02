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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class XmlUtilities {

    public static Stream<Node> stream(NodeList nodeList) {
        Node[] nodes = new Node[nodeList.getLength()];

        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes[i] = nodeList.item(i);
        }
        return Stream.of(nodes);
    }


    public static String printDocument(Document doc, boolean prettyPrint) throws TransformerException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        printDocument(doc, baos, prettyPrint);
        return new String(baos.toByteArray());
    }


    public static void printDocument(Document doc, OutputStream out, boolean prettyPrint) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        if (prettyPrint) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }


    public static DocumentBuilderFactory createNonValidatingDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        return factory;
    }
}
