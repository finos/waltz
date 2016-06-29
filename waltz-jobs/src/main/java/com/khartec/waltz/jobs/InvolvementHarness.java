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

import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.EntityIdSelectionOptions;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityIdSelectionOptions;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.involvement.InvolvementService;
import org.jooq.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static org.jooq.impl.DSL.select;


public class InvolvementHarness {

    private static String qry = "select \n" +
            "  [application].[name] \n" +
            "from [application]\n" +
            "where [application].[id] in (\n" +
            "  select [involvement].[entity_id]\n" +
            "  from [involvement]\n" +
            "  where (\n" +
            "    (\n" +
            "      [involvement].[employee_id] = 'Ms6tJhlJn'\n" +
            "      or [involvement].[employee_id] in (\n" +
            "        select [person_hierarchy].[employee_id]\n" +
            "        from [person_hierarchy]\n" +
            "        where [person_hierarchy].[manager_id] = 'Ms6tJhlJn'\n" +
            "      )\n" +
            "    )\n" +
            "    and [involvement].[entity_kind] = 'APPLICATION'\n" +
            "  )\n" +
            ")";

    public static void main(String[] args) throws InterruptedException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        DataSource dataSource = ctx.getBean(DataSource.class);
        InvolvementDao dao = ctx.getBean(InvolvementDao.class);
        InvolvementService service = ctx.getBean(InvolvementService.class);

        System.out.println("-- Waiting ...");
//        Thread.sleep(5000);

        System.out.println("-- Starting ...");
//        viaJdbc(dataSource);
//        viaJooqSql(dsl);
//        viaJooqOrig(dsl);
//        viaJooqJoins(dsl);
//        viaJooqSql(dsl);
//        viaDao(dao);
//        viaJdbc(dataSource);

        EntityIdSelectionOptions options = ImmutableEntityIdSelectionOptions.builder()
                .desiredKind(EntityKind.END_USER_APPLICATION)
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.PERSON)
                        .id(218)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        List<EndUserApplication> endUserApps = service.findAllEndUserApplicationsBySelector(options);

        System.out.println("got end user apps: " + endUserApps.size());
    }

    private static void viaDao(InvolvementDao dao) {
        System.out.println("-- dao start");
        long start = System.currentTimeMillis();


        List<Application> apps = dao.findAllApplicationsByEmployeeId("Ms6tJhlJn");
        System.out.println(apps.size());

        long duration = System.currentTimeMillis() - start;
        System.out.println("-- dao end "+ duration);

    }

    private static void viaJdbc(DataSource dataSource) {

        try (
            Connection conn = dataSource.getConnection();
        ){

            System.out.println("-- jdbc start");
            long start = System.currentTimeMillis();

            PreparedStatement pstmt = conn.prepareStatement(qry);
            ResultSet rs = pstmt.executeQuery();

            int c = 0;

            while(rs.next()) {
                c++;
            }

            System.out.println(c);
            long duration = System.currentTimeMillis() - start;
            System.out.println("-- jdbc end "+ duration);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viaJooqSql(DSLContext dsl) {
        System.out.println("-- jooq sql start");
        long st = System.currentTimeMillis();

        dsl.execute(qry);

        long dur = System.currentTimeMillis() - st;
        System.out.println("-- jooq sql end " + dur);

    }

    private static void viaJooqJoins(DSLContext dsl) {

        String employeeId = "Ms6tJhlJn";

        Condition eitherPersonOrReportee = PERSON_HIERARCHY.MANAGER_ID.eq(employeeId)
                .or(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId));

        SelectConditionStep<Record1<Long>> query = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .innerJoin(INVOLVEMENT)
                    .on(INVOLVEMENT.ENTITY_ID.eq(APPLICATION.ID))
                .innerJoin(PERSON_HIERARCHY)
                    .on(PERSON_HIERARCHY.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                    .and(eitherPersonOrReportee));

        System.out.println("-- jooq join start");
        long st = System.currentTimeMillis();

        System.out.println(query);

        Result<Record1<Long>> records = query.fetch();
        System.out.println(records.size());

        long dur = System.currentTimeMillis() - st;
        System.out.println("-- jooq join end " + dur);
    }

    private static void viaJooqOrig(DSLContext dsl) {

        String employeeId = "Ms6tJhlJn";

        SelectConditionStep<Record1<String>> allReporteesQuery = select(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId));

        SelectConditionStep<Record1<Long>> appIdsQuery = select(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId)
                        .or(INVOLVEMENT.EMPLOYEE_ID.in(allReporteesQuery))
                        .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

        SelectConditionStep<Record1<String>> query = dsl.select(APPLICATION.NAME)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(appIdsQuery));


        System.out.println("-- jooq orig start");
        long st = System.currentTimeMillis();

        query.fetch();

        long dur = System.currentTimeMillis() - st;
        System.out.println("-- jooq orig end " + dur);
    }

}
