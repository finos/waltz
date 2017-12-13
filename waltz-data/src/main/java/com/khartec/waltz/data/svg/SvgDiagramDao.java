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
                .build();
    };


    @Autowired
    public SvgDiagramDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<SvgDiagram> findByGroups(String[] groups) {
        return FunctionUtilities.time("SDD.findByGroups", () -> dsl.select()
                .from(SVG_DIAGRAM)
                .where(SVG_DIAGRAM.GROUP.in(groups))
                .orderBy(SVG_DIAGRAM.PRIORITY.asc())
                .fetch(svgMapper));
    }

}
