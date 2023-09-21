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

package org.finos.waltz.service.svg;

import org.finos.waltz.common.SvgUtilities;
import org.finos.waltz.data.svg.SvgDiagramDao;
import org.finos.waltz.model.svg.ImmutableSvgDiagram;
import org.finos.waltz.model.svg.SvgDiagram;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
public class SvgDiagramService {

    private final SvgDiagramDao svgDiagramDao;


    @Autowired
    public SvgDiagramService(SvgDiagramDao svgDiagramDao) {
        this.svgDiagramDao = svgDiagramDao;
    }


    public SvgDiagram getById(long id) {
        SvgDiagram diagram = svgDiagramDao.getById(id);
        return Unchecked.supplier(() -> {
            String updatedSvg = convertProductSpecificSvg(diagram);
            return ImmutableSvgDiagram.copyOf(diagram)
                    .withSvg(updatedSvg);
        }).get();
    }


    public Collection<SvgDiagram> findByGroups(String... groups) {
        return svgDiagramDao.findByGroups(groups)
                .stream()
                .map(Unchecked.function(diagram -> {
                    String updatedSvg = convertProductSpecificSvg(diagram);
                    return ImmutableSvgDiagram.copyOf(diagram)
                            .withSvg(updatedSvg);
                }))
                .collect(toList());
    }


    private String convertProductSpecificSvg(SvgDiagram diagram) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        switch (diagram.product()) {
            case "visio":
                return convertVisioSvg(diagram);
            default:
                return diagram.svg();
        }
    }


    private String convertVisioSvg(SvgDiagram diagram) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        String key = diagram.keyProperty();
        String svgStr = diagram.svg();

        return SvgUtilities.convertVisioSvg(key, svgStr);
    }


    public Set<SvgDiagram> findAll() {
        return svgDiagramDao.findAll();
    }


    public Boolean remove(long id) {
        return svgDiagramDao.remove(id);
    }


    public Boolean save(SvgDiagram diagram) {
        return svgDiagramDao.save(diagram);
    }
}