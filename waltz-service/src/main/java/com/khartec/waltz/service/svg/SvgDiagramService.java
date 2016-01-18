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
import com.khartec.waltz.model.svg.SvgDiagram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SvgDiagramService {

    private final SvgDiagramDao svgDiagramDao;

    @Autowired
    public SvgDiagramService(SvgDiagramDao svgDiagramDao) {
        this.svgDiagramDao = svgDiagramDao;
    }

    public List<SvgDiagram> findByKind(String kind) {
        return svgDiagramDao.findByKind(kind);
    }
}