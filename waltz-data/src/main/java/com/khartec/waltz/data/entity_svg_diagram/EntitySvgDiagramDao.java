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

package com.khartec.waltz.data.entity_svg_diagram;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import com.khartec.waltz.model.entity_svg_diagram.ImmutableEntitySvgDiagram;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntitySvgDiagram.ENTITY_SVG_DIAGRAM;


@Repository
public class EntitySvgDiagramDao {

    private final DSLContext dsl;


    @Autowired
    public EntitySvgDiagramDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntitySvgDiagram> findForEntityReference(EntityReference ref) {
        return dsl.selectFrom(ENTITY_SVG_DIAGRAM)
                .where(ENTITY_SVG_DIAGRAM.ENTITY_KIND.eq(ref.kind().name()))
                .and(ENTITY_SVG_DIAGRAM.ENTITY_ID.eq(ref.id()))
                .fetch(r -> ImmutableEntitySvgDiagram.builder()
                            .entityReference(ref)
                            .name(r.getName())
                            .description(r.getDescription())
                            .id(r.getId())
                            .externalId(Optional.ofNullable(r.getExternalId()))
                            .svg(r.getSvg())
                            .provenance(r.getProvenance())
                            .build());
    }

}
