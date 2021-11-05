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

package org.finos.waltz.data.entity_svg_diagram;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import org.finos.waltz.model.entity_svg_diagram.ImmutableEntitySvgDiagram;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.schema.tables.EntitySvgDiagram.ENTITY_SVG_DIAGRAM;
import static org.finos.waltz.common.Checks.checkNotNull;


@Repository
public class EntitySvgDiagramDao {

    private final DSLContext dsl;


    @Autowired
    public EntitySvgDiagramDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntitySvgDiagram> findForEntityReference(EntityReference ref) {
        return dsl
                .select(ENTITY_SVG_DIAGRAM.fields())
                .from(ENTITY_SVG_DIAGRAM)
                .where(ENTITY_SVG_DIAGRAM.ENTITY_KIND.eq(ref.kind().name()))
                .and(ENTITY_SVG_DIAGRAM.ENTITY_ID.eq(ref.id()))
                .fetch(r -> ImmutableEntitySvgDiagram.builder()
                            .entityReference(ref)
                            .name(r.get(ENTITY_SVG_DIAGRAM.NAME))
                            .description(r.get(ENTITY_SVG_DIAGRAM.DESCRIPTION))
                            .id(r.get(ENTITY_SVG_DIAGRAM.ID))
                            .externalId(Optional.ofNullable(r.get(ENTITY_SVG_DIAGRAM.EXTERNAL_ID)))
                            .svg(r.get(ENTITY_SVG_DIAGRAM.SVG))
                            .provenance(r.get(ENTITY_SVG_DIAGRAM.PROVENANCE))
                            .build());
    }

}
