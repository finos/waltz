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

import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;


public class AppGroupHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AppGroupDao dao = ctx.getBean(AppGroupDao.class);

        EntityRelationshipDao relDao = ctx.getBean(EntityRelationshipDao.class);

        System.out.println("--- start");

        EntityReference grp5 = mkRef(EntityKind.APP_GROUP, 5);
        EntityReference grp3 = mkRef(EntityKind.APP_GROUP, 3);

        EntityRelationship relationship = ImmutableEntityRelationship
                .builder()
                .a(grp5)
                .b(grp3)
                .relationship(RelationshipKind.RELATES_TO)
                .lastUpdatedBy("admin")
                .build();

        relDao.create(relationship);

        List<AppGroup> groups = dao.findRelatedByEntityReferenceAndUser(
                grp5,
                "admin");

        groups.forEach(System.out::println);

        System.out.println("--- done");



    }

}
