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

package com.khartec.waltz.data.svg;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.model.svg.ImmutableSvgDiagram;
import com.khartec.waltz.model.svg.SvgDiagram;
import com.khartec.waltz.schema.tables.records.SvgDiagramRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.SvgDiagram.SVG_DIAGRAM;

@Repository
public class SvgDiagramDao {

    private final DSLContext dsl;


    private static RecordMapper<Record, SvgDiagram> svgMapper = r -> {
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
                .fetchOne(svgMapper);
    }


    public List<SvgDiagram> findByGroups(String[] groups) {
        return FunctionUtilities.time("SDD.findByGroups", () -> dsl.select()
                .from(SVG_DIAGRAM)
                .where(SVG_DIAGRAM.GROUP.in(groups))
                .orderBy(SVG_DIAGRAM.PRIORITY.asc())
                .fetch(svgMapper));
    }

}
