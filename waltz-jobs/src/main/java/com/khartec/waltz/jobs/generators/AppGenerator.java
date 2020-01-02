/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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

import static com.khartec.waltz.common.RandomUtilities.randomPick;
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
