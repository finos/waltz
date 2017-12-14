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

import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;


public class ChangeInitiativeHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);
        ChangeInitiativeDao dao = ctx.getBean(ChangeInitiativeDao.class);

        ChangeInitiative changeInitiative = dao.getById(1L);
        System.out.println(changeInitiative);



        SelectConditionStep<Record1<Long>> ouHier = dsl
                .selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ID.eq(210L)
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())));


        SelectConditionStep<Record1<Long>> baseIdSelector = dsl
                .selectDistinct(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ORGANISATIONAL_UNIT_ID.in(ouHier));


        SelectConditionStep<Record1<Long>> ciHier = dsl
                .selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ID.in(baseIdSelector)
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())));


        dsl.select(CHANGE_INITIATIVE.NAME)
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(ciHier))
                .forEach(System.out::println);

    }

}
