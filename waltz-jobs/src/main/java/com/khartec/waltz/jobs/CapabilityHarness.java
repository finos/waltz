/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.data.capability.CapabilityIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.ImmutableIdSelectionOptions;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;


public class CapabilityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        CapabilityDao dao = ctx.getBean(CapabilityDao.class);


        CapabilityIdSelectorFactory selectorFactory = ctx.getBean(CapabilityIdSelectorFactory.class);

        Select<Record1<Long>> selector = selectorFactory.apply(ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.APP_GROUP)
                        .id(5L)
                        .build())
                .scope(HierarchyQueryScope.PARENTS)
                .build());

        Collection<Capability> caps = dao.findByIdSelector(selector);
        caps.forEach(c -> System.out.println(c.name()));

    }

}
