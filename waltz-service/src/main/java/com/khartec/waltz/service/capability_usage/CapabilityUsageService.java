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

package com.khartec.waltz.service.capability_usage;

import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.applicationcapability.CapabilityUsage;
import com.khartec.waltz.model.applicationcapability.ImmutableCapabilityUsage;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.capabilityrating.CapabilityRating;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.capability_rating.CapabilityRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CapabilityUsageService {

    private final CapabilityService capabilityService;
    private final CapabilityRatingService capabilityRatingService;
    private final AppCapabilityService appCapabilityService;

    @Autowired
    public CapabilityUsageService(AppCapabilityService appCapabilityService,
                                  CapabilityService capabilityService,
                                  CapabilityRatingService capabilityRatingService) {
        this.appCapabilityService = appCapabilityService;
        this.capabilityService = capabilityService;
        this.capabilityRatingService = capabilityRatingService;
    }


    public CapabilityUsage getUsage(long capabilityId) {

        Forest<Capability, Long> forest = capabilityService.buildHierarchy();

        Node<Capability, Long> capNode = forest.getAllNodes().get(capabilityId);


        List<Long> capIds = IdUtilities.toIds(HierarchyUtilities.flatten(capNode));

        List<ApplicationCapability> appCapabilities = appCapabilityService.findByCapabilityIds(capIds);
        List<CapabilityRating> ratings = capabilityRatingService.findByCapabilityIds(capIds);


        return classifyAppCapabilities(capNode, appCapabilities, ratings);
    }

    private static CapabilityUsage classifyAppCapabilities(Node<Capability, Long> capNode,
                                                           List<ApplicationCapability> appCapabilities,
                                                           List<CapabilityRating> ratings)
    {
        return ImmutableCapabilityUsage.builder()
                .capability(capNode.getData())
                .usages(appCapabilities
                        .stream()
                        .filter(ac -> ac.capabilityId() == capNode.getId())
                        .collect(Collectors.toList()))
                .ratings(ratings.stream()
                        .filter(r -> r.capability().id() == capNode.getId())
                        .collect(Collectors.toList()))
                .children(capNode.getChildren()
                        .stream()
                        .map(n -> classifyAppCapabilities(n, appCapabilities, ratings))
                        .collect(Collectors.toList()))
                .build();

    }


}
