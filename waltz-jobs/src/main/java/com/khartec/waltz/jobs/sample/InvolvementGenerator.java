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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.MapBuilder;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.involvement.ImmutableInvolvement;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.involvement.InvolvementKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ListUtilities.randomPick;


public class InvolvementGenerator {


    private static final Random rnd = new Random();


    public static void main(String[] args) {

//        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
//
//        DataSource ds = ctx.getBean(DataSource.class);
//        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(ds);
//
//        List<String> developers = template.queryForList("SELECT employee_id FROM person WHERE title LIKE '%developer%'", new HashMap<>(), String.class);
//        List<String> managers = template.queryForList("SELECT employee_id FROM person WHERE title LIKE '%manager%'", new HashMap<>(), String.class);
//        List<String> analysts = template.queryForList("SELECT employee_id FROM person WHERE title LIKE '%analyst%'", new HashMap<>(), String.class);
//        List<String> administrators = template.queryForList("SELECT employee_id FROM person WHERE title LIKE '%administrator%'", new HashMap<>(), String.class);
//        List<String> qa = template.queryForList("SELECT employee_id FROM person WHERE title LIKE '%qa%'", new HashMap<>(), String.class);
//
//        List<String> directors = template.queryForList("SELECT employee_id FROM person WHERE title LIKE '%director%' AND title NOT LIKE '%sales%'", new HashMap<>(), String.class);
//
//        List<Long> orgUnitIds = template.queryForList("SELECT id FROM organisational_unit", new HashMap<>(), Long.class);
//
//
//        List<Long> inHouseApps = template.queryForList("SELECT id FROM application WHERE kind = 'IN_HOUSE'", new HashMap<>(), Long.class);
//        List<Long> hostedApps = template.queryForList("SELECT id FROM application WHERE kind = 'INTERNALLY_HOSTED'", new HashMap<>(), Long.class);
//        List<Long> externalApps = template.queryForList("SELECT id FROM application WHERE kind = 'EXTERNALLY_HOSTED'", new HashMap<>(), Long.class);
//        List<Long> eucApps = template.queryForList("SELECT id FROM application WHERE kind = 'EUC'", new HashMap<>(), Long.class);
//
//        List<Involvement> devInvolvements = inHouseApps.stream()
//                .map(id -> toAppRef(id))
//                .flatMap(appRef -> mkInvolvments(appRef, developers, InvolvementKind.DEVELOPER, 7))
//                .collect(Collectors.toList());
//
//        List<Involvement> qaInvolvements = concat(inHouseApps, hostedApps)
//                .stream()
//                .map(id -> toAppRef(id))
//                .flatMap(appRef -> mkInvolvments(appRef, qa, InvolvementKind.QA, 3))
//                .collect(Collectors.toList());
//
//        List<Involvement> projectManagerInvolvements = concat(inHouseApps, externalApps, hostedApps, eucApps)
//                .stream()
//                .map(id -> toAppRef(id))
//                .flatMap(appRef -> mkInvolvments(appRef, managers, InvolvementKind.PROJECT_MANAGER, 1))
//                .collect(Collectors.toList());
//
//        List<Involvement> supportManagerInvolvments = concat(inHouseApps, externalApps, hostedApps)
//                .stream()
//                .map(id -> toAppRef(id))
//                .flatMap(appRef -> mkInvolvments(appRef, managers, InvolvementKind.SUPPORT_MANAGER, 1))
//                .collect(Collectors.toList());
//
//        List<Involvement> analystInvolvments = concat(inHouseApps, externalApps, hostedApps)
//                .stream()
//                .map(id -> toAppRef(id))
//                .flatMap(appRef -> mkInvolvments(appRef, analysts, InvolvementKind.BUSINESS_ANALYST, 3))
//                .collect(Collectors.toList());
//
//        List<Involvement> ouArchitects = orgUnitIds.stream()
//                .map(id -> ImmutableInvolvement.builder()
//                        .employeeId(randomPick(directors))
//                        .entityReference(ImmutableEntityReference.builder().kind(EntityKind.ORG_UNIT).id(id).build())
//                        .kind(InvolvementKind.IT_ARCHITECT)
//                        .build())
//                .collect(Collectors.toList());
//
//        List<Involvement> ouSponsors = orgUnitIds.stream()
//                .map(id -> ImmutableInvolvement.builder()
//                        .employeeId(randomPick(directors))
//                        .entityReference(ImmutableEntityReference.builder().kind(EntityKind.ORG_UNIT).id(id).build())
//                        .kind(InvolvementKind.BUSINESS_SPONSOR)
//                        .build())
//                .collect(Collectors.toList());
//
//        template.update("DELETE FROM involvement", new HashMap<>());
//
//        String insertSql = "INSERT INTO involvement(entity_kind, entity_id, kind, employee_id) " +
//                " VALUES (:entityKind, :entityId, :kind, :employeeId)";
//
//        template.batchUpdate(insertSql, toParams(devInvolvements));
//        template.batchUpdate(insertSql, toParams(qaInvolvements));
//        template.batchUpdate(insertSql, toParams(supportManagerInvolvments));
//        template.batchUpdate(insertSql, toParams(projectManagerInvolvements));
//        template.batchUpdate(insertSql, toParams(analystInvolvments));
//        template.batchUpdate(insertSql, toParams(ouArchitects));
//        template.batchUpdate(insertSql, toParams(ouSponsors));

        System.out.println("Done");

    }

    private static Stream<Involvement> mkInvolvments(EntityReference appRef, List<String> employeeIds, InvolvementKind kind, int upperBound) {
        int count = rnd.nextInt(upperBound) + 1;
        List<Involvement> involvements = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Involvement involvement = ImmutableInvolvement.builder()
                    .kind(kind)
                    .employeeId(randomPick(employeeIds))
                    .entityReference(appRef)
                    .build();
            involvements.add(involvement);
        }
        return involvements.stream();
    }


    private static Map[] toParams(List<Involvement> involvements) {
        return  involvements.stream()
                .map(i -> new MapBuilder()
                        .add("entityKind", i.entityReference().kind().name())
                        .add("entityId", i.entityReference().id())
                        .add("kind", i.kind().name())
                        .add("employeeId", i.employeeId())
                        .build())
                .toArray(Map[]::new);
    }


    private static EntityReference toAppRef(Long id) {
        return ImmutableEntityReference.builder()
                .id(id)
                .kind(EntityKind.APPLICATION)
                .build();
    }

}
