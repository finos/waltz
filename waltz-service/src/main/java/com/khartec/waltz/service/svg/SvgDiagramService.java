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

import com.khartec.waltz.common.SvgUtilities;
import com.khartec.waltz.data.svg.SvgDiagramDao;
import com.khartec.waltz.model.svg.ImmutableSvgDiagram;
import com.khartec.waltz.model.svg.SvgDiagram;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;

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
            return convertVisioSvg(diagram);
        } else {
            return diagram.svg();
        }
    }


    private String convertVisioSvg(SvgDiagram diagram) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        String key = diagram.keyProperty();
        String svgStr = diagram.svg();

        return SvgUtilities.convertVisioSvg(key, svgStr);
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