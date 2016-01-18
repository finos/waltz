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

import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.model.application.AppRegistrationRequest;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableAppRegistrationRequest;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;


public class AppGenerator2 {

    private static final Random rnd = new Random();

    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        OrganisationalUnitService ouDao = ctx.getBean(OrganisationalUnitService.class);

        List<String> animals = IOUtilities.readLines(AppGenerator2.class.getClassLoader().getResourceAsStream("animals.txt"));
        OrganisationalUnit[] organisationalUnits = ouDao.findAll().toArray(new OrganisationalUnit[0]);


        List<AppRegistrationRequest> registrationRequests = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            String animal = randomPick(animals.toArray(new String[0]));
            animals.remove(animal);


            OrganisationalUnit organisationalUnit = randomPick(organisationalUnits);

            LifecyclePhase phase = rnd.nextInt(10) > 7
                    ? randomPick(LifecyclePhase.values())
                    : LifecyclePhase.PRODUCTION;

            AppRegistrationRequest app = ImmutableAppRegistrationRequest.builder()
                    .name(animal)
                    .assetCode("wltz-0" + i)
                    .description("All about " + animal)
                    .kind(randomPick(ApplicationKind.values()))
                    .lifecyclePhase(phase)
                    .organisationalUnitId(organisationalUnit.id().get())
                    .build();

            registrationRequests.add(app);
        }

        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);

        registrationRequests.forEach(applicationDao::registerApp);

    }
}
