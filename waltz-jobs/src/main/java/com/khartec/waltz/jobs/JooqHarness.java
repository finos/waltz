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

import com.khartec.waltz.data.app_capability.AppCapabilityDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.bookmark.BookmarkDao;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.data.capability_rating.CapabilityRatingDao;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.capabilityrating.CapabilityRating;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.person.PersonService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class JooqHarness {


    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        ApplicationDao appDao = ctx.getBean(ApplicationDao.class);
        ApplicationService appSvc = ctx.getBean(ApplicationService.class);
        DataFlowDao dfDao = ctx.getBean(DataFlowDao.class);
        CapabilityRatingDao capRatDao = ctx.getBean(CapabilityRatingDao.class);
        OrganisationalUnitDao orgDao = ctx.getBean(OrganisationalUnitDao.class);
        AppCapabilityDao appCapDao = ctx.getBean(AppCapabilityDao.class);
        CapabilityDao capDao = ctx.getBean(CapabilityDao.class);
        BookmarkDao bookmarkDao = ctx.getBean(BookmarkDao.class);
        PersonService personDao = ctx.getBean(PersonService.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);


        int FRONT_OFFICE = 260;  // app 552
        int EQUITIES = 270;  // app 669


//        appCapDao.findApplicationCapabilitiesForOrgUnit(400).forEach(System.out::println);
//        System.out.println();
//        System.out.println();
//        appCapDao.findCapabilitiesForApp(594).forEach(System.out::println);
//        System.out.println();
//        System.out.println();
//
//        ImmutableGroupedApplications grouped = appCapDao.findGroupedApplicationsByCapability(1200L);
//
//        grouped.primaryApps().forEach(System.out::println);
//        System.out.println("2222222222222");
//        grouped.secondaryApps().forEach(System.out::println);
////
//
//        appCapDao.tallyByCapabilityId().forEach(System.out::println);
//
//        appCapDao.addCapabilitiesToApp(2010L, ListUtilities.newArrayList(999L));
//
//
//        List<Capability> descendants = capDao.findDescendants(3000);
//        List<Long> ids = toIds(descendants);
//        System.out.println(ids);

//        Bookmark r = bookmarkDao.create(ImmutableBookmark.builder()
//                .title("test")
//                .parent(ImmutableEntityReference.builder()
//                        .id(1)
//                        .kind(EntityKind.APPLICATION)
//                        .build())
//                .kind(BookmarkKind.APPLICATION_INSTANCE)
//                .description("test desc")
//                .build());
//
//        System.out.println(r);


        List<Person> ellasMgrs = personDao.findAllManagersByEmployeeId("dvkDz0djp");
        ellasMgrs.forEach(m -> System.out.println(m.displayName()));
    }



    private static void pretty(CapabilityRating cr) {
        String msg = "app: " + cr.parent().id() + ", capability: " + cr.capability().name() + ", rating: "+cr.ragRating();
        System.out.println(msg);
    }


    private static void pretty(OrganisationalUnit ou) {
        String msg = "id: " + ou.id() + ", name: " + ou.name();
        System.out.println(msg);
    }

}
