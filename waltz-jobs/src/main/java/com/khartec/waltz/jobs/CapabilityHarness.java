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

import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.capability.ImmutableCapability;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.capability_usage.CapabilityUsageService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class CapabilityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        CapabilityUsageService capabilityUsageService = ctx.getBean(CapabilityUsageService.class);
        CapabilityService capabilityService = ctx.getBean(CapabilityService.class);

        capabilityService.assignLevels();

        Forest<Capability, Long> forest = capabilityService.buildHierarchy();

        for (Node<Capability, Long> node : forest.getAllNodes().values()) {
            Capability capability = node.getData();
            int currLevel = capability.level();

            Node ptr = node;
            while (ptr != null) {
                capability = setLevel(capability, currLevel, ptr);
                ptr = ptr.getParent();
                currLevel--;
            }

            capabilityService.update(capability);
        }


    }

    private static Capability setLevel(Capability capability, int level, Node<Capability, Long> node) {
        if (level == 1) {
            return ImmutableCapability.copyOf(capability).withLevel1(node.getId());
        }
        if (level == 2) {
            return ImmutableCapability.copyOf(capability).withLevel2(node.getId());
        }
        if (level == 3) {
            return ImmutableCapability.copyOf(capability).withLevel3(node.getId());
        }
        if (level == 4) {
            return ImmutableCapability.copyOf(capability).withLevel4(node.getId());
        }
        if (level == 5) {
            return ImmutableCapability.copyOf(capability).withLevel5(node.getId());
        }
        return capability;
    }


}
