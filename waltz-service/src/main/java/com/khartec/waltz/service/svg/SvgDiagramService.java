/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.svg;

import com.khartec.waltz.data.svg.SvgDiagramDao;
import com.khartec.waltz.model.svg.ImmutableSvgDiagram;
import com.khartec.waltz.model.svg.SvgDiagram;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.XmlUtilities.createNonValidatingDocumentBuilderFactory;
import static com.khartec.waltz.common.XmlUtilities.printDocument;
import static com.khartec.waltz.common.XmlUtilities.stream;
import static java.util.stream.Collectors.toList;

@Service
public class SvgDiagramService {

    private final SvgDiagramDao svgDiagramDao;

    @Autowired
    public SvgDiagramService(SvgDiagramDao svgDiagramDao) {
        this.svgDiagramDao = svgDiagramDao;
    }


    private String convertProductSpecificSvg(SvgDiagram diagram) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        if (diagram.product().equals("visio")) {
            return convertVisoSvg(diagram);
        } else {
            return diagram.svg();
        }
    }


    private String convertVisoSvg(SvgDiagram diagram) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        DocumentBuilder builder = createNonValidatingDocumentBuilderFactory().newDocumentBuilder();
        InputSource svgSource = new InputSource(new ByteArrayInputStream(diagram.svg().getBytes()));
        Document svg = builder.parse(svgSource);

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//*", svg, XPathConstants.NODESET);

        String key = diagram.keyProperty();

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

        return printDocument(svg);
    }


    public List<SvgDiagram> findByKind(String kind) {
        return svgDiagramDao.findByKind(kind)
                .stream()
                .map(Unchecked.function(diag -> {
                        String updatedSvg = convertProductSpecificSvg(diag);
                        return ImmutableSvgDiagram.copyOf(diag)
                                .withSvg(updatedSvg);
                }))
                .collect(toList());
    }

}