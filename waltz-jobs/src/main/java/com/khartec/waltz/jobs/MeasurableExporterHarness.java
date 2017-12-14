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

package com.khartec.waltz.jobs;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.Tables.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.Person.PERSON;

public class MeasurableExporterHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        long categoryId = 1L;

        SelectConditionStep<Record> qry = dsl
                .select(MEASURABLE.NAME, MEASURABLE.DESCRIPTION, MEASURABLE.ID, MEASURABLE.PARENT_ID, MEASURABLE.EXTERNAL_ID)
                .select(INVOLVEMENT_KIND.NAME)
                .select(PERSON.DISPLAY_NAME)
                .from(MEASURABLE)
                .leftJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(MEASURABLE.ID).and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .leftJoin(INVOLVEMENT_KIND)
                .on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                .leftJoin(PERSON)
                .on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId));

        qry.fetch().forEach(System.out::println);

    }
}
