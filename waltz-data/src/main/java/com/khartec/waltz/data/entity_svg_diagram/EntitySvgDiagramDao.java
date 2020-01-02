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
