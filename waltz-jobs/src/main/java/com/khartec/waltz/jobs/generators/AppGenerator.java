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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.application.AppRegistrationRequest;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableAppRegistrationRequest;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;


public class AppGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);
        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        OrganisationalUnitService ouDao = ctx.getBean(OrganisationalUnitService.class);

        List<String> animals = Unchecked.supplier(() -> readLines(getClass().getResourceAsStream("/app-names.txt"))).get();

        OrganisationalUnit[] organisationalUnits = ouDao.findAll().toArray(new OrganisationalUnit[0]);

        List<AppRegistrationRequest> registrationRequests = new ArrayList<>();

        for (int i = 0; i < NUM_APPS; i++) {
            String animal = randomPick(animals.toArray(new String[0])) + " - " + i;


            OrganisationalUnit organisationalUnit = randomPick(organisationalUnits);

            LifecyclePhase phase = rnd.nextInt(10) > 7
                    ? randomPick(LifecyclePhase.values())
                    : LifecyclePhase.PRODUCTION;

            Criticality businessCriticality = rnd.nextInt(10) > 7
                    ? randomPick(Criticality.values())
                    : Criticality.HIGH;

            AppRegistrationRequest app = ImmutableAppRegistrationRequest.builder()
                    .name(animal)
                    .assetCode("wltz-0" + i)
                    .description("All about " + animal)
                    .applicationKind(randomPick(ApplicationKind.values()))
                    .lifecyclePhase(phase)
                    .overallRating(randomPick(RagRating.R, RagRating.A, RagRating.A, RagRating.G, RagRating.G))
                    .organisationalUnitId(organisationalUnit.id().get())
                    .businessCriticality(businessCriticality)
                    .build();

            registrationRequests.add(app);
        }

        registrationRequests.forEach(a -> applicationDao.registerApp(a, "admin"));

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        dsl.deleteFrom(AUTHORITATIVE_SOURCE).execute();
        dsl.deleteFrom(APPLICATION).execute();
        return true;
    }
}
