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

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.data.app_capability.AppCapabilityDao;
import com.khartec.waltz.model.application_capability.ImmutableApplicationCapability;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.capability.CapabilityService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;

@Deprecated
public class AppCapabilityGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        CapabilityService capabilityDao = ctx.getBean(CapabilityService.class);
        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        AppCapabilityDao appCapabilityDao = ctx.getBean(AppCapabilityDao.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        dsl.delete(APP_CAPABILITY)
                .where(APP_CAPABILITY.PROVENANCE.eq("test"))
                .execute();

        List<Capability> capabilities = capabilityDao.findAll();

        applicationDao
                .findAll()
                .forEach(app -> {
                    int count = rnd.nextInt(4) + 1;
                    IntStream
                            .range(0, count)
                            .mapToObj(i -> randomPick(capabilities))
                            .map(c -> c.id().get())
                            .collect(Collectors.toSet())
                            .stream()
                            .map(cid -> ImmutableApplicationCapability.builder()
                                    .applicationId(app.id().get())
                                    .capabilityId(cid)
                                    .rating(ArrayUtilities.randomPick(RagRating.values()))
                                    .description("sample data")
                                    .provenance("test")
                                    .lastUpdatedBy("admin")
                                    .isPrimary(false)
                                    .build())
                            .forEach(appCapabilityDao::insert);
                });

    }
}
