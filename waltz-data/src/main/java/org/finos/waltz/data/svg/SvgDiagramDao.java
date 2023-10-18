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

package org.finos.waltz.data.svg;

import org.finos.waltz.model.svg.ImmutableSvgDiagram;
import org.finos.waltz.model.svg.SvgDiagram;
import org.finos.waltz.schema.tables.records.SvgDiagramRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.schema.tables.SvgDiagram.SVG_DIAGRAM;

@Repository
public class SvgDiagramDao {

    private final DSLContext dsl;


    private static final RecordMapper<Record, SvgDiagram> SVG_MAPPER = r -> {
        SvgDiagramRecord record = r.into(SVG_DIAGRAM);
        return ImmutableSvgDiagram.builder()
                .id(record.getId())
                .description(mkSafe(record.getDescription()))
                .keyProperty(record.getKeyProperty())
                .name(record.getName())
                .priority(record.getPriority())
                .group(record.getGroup())
                .svg(record.getSvg())
                .product(record.getProduct())
                .displayWidthPercent(record.getDisplayWidthPercent())
                .displayHeightPercent(record.getDisplayHeightPercent())
                .build();
    };


    @Autowired
    public SvgDiagramDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public SvgDiagram getById(long id) {
        return dsl.select()
                .from(SVG_DIAGRAM)
                .where(SVG_DIAGRAM.ID.eq(id))
                .fetchOne(SVG_MAPPER);
    }


    public List<SvgDiagram> findByGroups(String[] groups) {
        return dsl
                .select()
                .from(SVG_DIAGRAM)
                .where(SVG_DIAGRAM.GROUP.in(groups))
                .orderBy(SVG_DIAGRAM.PRIORITY.asc())
                .fetch(SVG_MAPPER);
    }


    public Set<SvgDiagram> findAll() {
        return dsl
                .select(SVG_DIAGRAM.fields())
                .from(SVG_DIAGRAM)
                .fetchSet(SVG_MAPPER);
    }


    public Boolean remove(long id) {
        return dsl
                .deleteFrom(SVG_DIAGRAM)
                .where(SVG_DIAGRAM.ID.eq(id))
                .execute() == 1;
    }


    public Boolean save(SvgDiagram diagram) {

        SvgDiagramRecord record = dsl.newRecord(SVG_DIAGRAM);
        record.setName(diagram.name());
        record.setGroup(diagram.group());
        record.setDescription(diagram.description());
        record.setSvg(diagram.svg());
        record.setKeyProperty(diagram.keyProperty());
        record.setProduct(diagram.product());
        record.setPriority(diagram.priority());
        record.setDisplayHeightPercent(diagram.displayHeightPercent());
        record.setDisplayWidthPercent(diagram.displayWidthPercent());

        diagram.id().ifPresent(id -> {
            record.setId(id);
            record.changed(SVG_DIAGRAM.ID, false);
        });

        int store = record.store();
        return store == 1;
    }
}
