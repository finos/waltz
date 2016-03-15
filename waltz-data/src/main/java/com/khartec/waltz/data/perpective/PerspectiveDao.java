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

package com.khartec.waltz.data.perpective;

import com.khartec.waltz.model.perspective.ImmutableMeasurable;
import com.khartec.waltz.model.perspective.ImmutablePerspective;
import com.khartec.waltz.model.perspective.Measurable;
import com.khartec.waltz.model.perspective.Perspective;
import com.khartec.waltz.schema.tables.records.PerspectiveMeasurableRecord;
import com.khartec.waltz.schema.tables.records.PerspectiveRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.schema.tables.Perspective.PERSPECTIVE;
import static com.khartec.waltz.schema.tables.PerspectiveMeasurable.PERSPECTIVE_MEASURABLE;


@Repository
public class PerspectiveDao {


    private final DSLContext dsl;


    @Autowired
    public PerspectiveDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Perspective getPerspective(String code) {
        Perspective basePerspective = getBasePerspective(code);
        Iterable<Measurable> measurables = getMeasurables(code);

        return ImmutablePerspective
                .copyOf(basePerspective)
                .withMeasurables(measurables);
    }


    private Iterable<Measurable> getMeasurables(String code) {
        return dsl.select(PERSPECTIVE_MEASURABLE.fields())
                .from(PERSPECTIVE_MEASURABLE)
                .where(PERSPECTIVE_MEASURABLE.PERSPECTIVE_CODE.eq(code))
                .orderBy(PERSPECTIVE_MEASURABLE.NAME.asc())
                .fetch(r -> {
                    PerspectiveMeasurableRecord record = r.into(PERSPECTIVE_MEASURABLE);
                    return ImmutableMeasurable.builder()
                            .name(record.getName())
                            .code(record.getCode())
                            .description(record.getDescription())
                            .build();
                });
    }


    private Perspective getBasePerspective(String code) {
        return dsl.select(PERSPECTIVE.fields())
                .from(PERSPECTIVE)
                .where(PERSPECTIVE.CODE.eq(code))
                .fetchOne(r -> {
                    PerspectiveRecord record = r.into(PERSPECTIVE);
                    return ImmutablePerspective.builder()
                            .name(record.getName())
                            .code(record.getCode())
                            .description(record.getDescription())
                            .build();
                });
    }

}
