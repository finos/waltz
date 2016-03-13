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

import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.applicationcapability.ImmutableAppCapabilityUsage;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.schema.tables.AppCapability;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.capability.CapabilityService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.OptionalUtilities.toList;


public class AppCapabilityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AppCapabilityService appCapabilityService = ctx.getBean(AppCapabilityService.class);
        CapabilityService capabilityService = ctx.getBean(CapabilityService.class);

        long appId = 98;

        List<ApplicationCapability> appCapabilities = appCapabilityService.findByAppIds(appId);

        List<Long> explicitCapabilityIds = appCapabilities.stream()
                .map(ac -> ac.capabilityId())
                .collect(Collectors.toList());


        List<Capability> explicitCapabilities = capabilityService.findByIds(explicitCapabilityIds.toArray(new Long[0]));

        Set<Long> allIds = explicitCapabilities.stream()
                .map(c -> toList(c.level1(), c.level2(), c.level3(), c.level4(), c.level5()))
                .flatMap(ids -> ids.stream())
                .collect(Collectors.toSet());

        List<Capability> allCapabilities = capabilityService.findByIds(allIds.toArray(new Long[0]));


        ImmutableAppCapabilityUsage.builder()
                .applicationId(appId)
                .explicitCapabilityIds(explicitCapabilityIds)
                .allCapabilities(allCapabilities)
                .build();


        allCapabilities.forEach(System.out::println);


    }

}
