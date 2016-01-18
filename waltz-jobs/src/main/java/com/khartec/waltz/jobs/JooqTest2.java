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

package com.khartec.waltz.jobs;

import com.khartec.waltz.service.DIConfiguration;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.PerspectiveRating.PERSPECTIVE_RATING;
import static org.jooq.impl.DSL.*;


/**
 * Created by dwatkins on 19/10/2015.
 */
public class JooqTest2 {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DataSource dataSource = ctx.getBean(DataSource.class);

        DSLContext dsl = DSL.using(
                dataSource,
                SQLDialect.POSTGRES_9_4,
                new Settings().withRenderFormatted(true));



        Table<Record> orgTree = table(name("orgTree"));
        Field<Long> ouIdField = field(name("orgTree", "ouId"), Long.class);

        SelectJoinStep<Record1<Long>> with = dsl.withRecursive("orgTree", "ouId")
                .as(select(ORGANISATIONAL_UNIT.ID)
                    .from(ORGANISATIONAL_UNIT).where(ORGANISATIONAL_UNIT.ID.eq(Long.valueOf(210)))
                        .unionAll(
                                select(ORGANISATIONAL_UNIT.ID)
                                        .from(ORGANISATIONAL_UNIT, orgTree)
                                        .where(ORGANISATIONAL_UNIT.PARENT_ID.eq(ouIdField))))
                .select(ouIdField).from(orgTree);


        Field[] fields = new Field[] {APPLICATION.NAME, APPLICATION.ORGANISATIONAL_UNIT_ID, PERSPECTIVE_RATING.CAPABILITY_ID, PERSPECTIVE_RATING.RATING};
        String sql = dsl.select(fields)
                .from(PERSPECTIVE_RATING)
                .innerJoin(APPLICATION)
                .on(PERSPECTIVE_RATING.PARENT_ID.eq(APPLICATION.ID))
                .where(PERSPECTIVE_RATING.PERSPECTIVE_CODE.eq("BUSINESS"))
                .and(APPLICATION.ORGANISATIONAL_UNIT_ID.in(with))
                .getSQL()
                ;




        System.out.println(sql);





    }


    public static final String Q = "WITH RECURSIVE orgTree(ouId, pId, name, description) AS (\n" +
            "  SELECT id, parent_id, name, description FROM organisational_unit WHERE id = 210\n" +
            "  UNION ALL\n" +
            "  SELECT ou.id, ou.parent_id, ou.name, ou.description\n" +
            "  FROM organisational_unit ou, orgTree t\n" +
            "  WHERE ou.parent_id = t.ouId\n" +
            ")\n" +
            "SELECT * FROM orgTree\n" +
            "LIMIT 10\n" +
            ";\n";
}
