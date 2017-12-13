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

package com.khartec.waltz.service.entity_svg_diagram;

import com.khartec.waltz.data.entity_svg_diagram.EntitySvgDiagramDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntitySvgDiagramService {

    private final EntitySvgDiagramDao entitySvgDiagramDao;


    @Autowired
    public EntitySvgDiagramService(EntitySvgDiagramDao entitySvgDiagramDao) {
        checkNotNull(entitySvgDiagramDao, "entitySvgDiagramDao cannot be null");
        this.entitySvgDiagramDao = entitySvgDiagramDao;
    }


    public List<EntitySvgDiagram> findForEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return entitySvgDiagramDao.findForEntityReference(ref);
    }

}
