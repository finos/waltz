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

package com.khartec.waltz.service.app_capability;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.app_capability.AppCapabilityDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.EntityReferenceKeyedGroup;
import com.khartec.waltz.model.ImmutableEntityReferenceKeyedGroup;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.applicationcapability.GroupedApplications;
import com.khartec.waltz.model.tally.Tally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AppCapabilityService {

    private final AppCapabilityDao dao;


    @Autowired
    public AppCapabilityService(AppCapabilityDao appCapabilityDao) {
        checkNotNull(appCapabilityDao, "dao must not be null");

        this.dao = appCapabilityDao;
    }


    public List<ApplicationCapability> findCapabilitiesForApp(long appId) {
        return dao.findCapabilitiesForApp(appId);
    }


    public GroupedApplications findGroupedApplicationsByCapability(long capabilityId) {
        return dao.findGroupedApplicationsByCapability(capabilityId);
    }


    public List<ApplicationCapability> findAssociatedApplicationCapabilities(long capabilityId) {
        return dao.findAssociatedApplicationCapabilities(capabilityId);
    }


    public List<EntityReferenceKeyedGroup> findAssociatedCapabilitiesByApplication(long applicationId) {
        List<ApplicationCapability> capabilitiesForApp = findCapabilitiesForApp(applicationId);

        List<Long> existingCapabilityIds = ListUtilities.map(capabilitiesForApp, ac -> ac.capabilityReference().id());

        List<ApplicationCapability> associated = capabilitiesForApp.stream()
                .map(appCapability -> appCapability.capabilityReference().id())
                .flatMap(capabilityId -> findAssociatedApplicationCapabilities(capabilityId).stream())
                .filter(associatedAppCap -> associatedAppCap.applicationReference().id() != applicationId)
                .filter(associatedAppCap -> ! existingCapabilityIds.contains(associatedAppCap.capabilityReference().id()))
                .collect(Collectors.toList());

        Map<EntityReference, Collection<EntityReference>> associatedCapabilitiesToApps = MapUtilities.groupBy(
                appCap -> appCap.capabilityReference(),
                appCap -> appCap.applicationReference(),
                associated);

        List<EntityReferenceKeyedGroup> result = ListUtilities.newArrayList();
        associatedCapabilitiesToApps.forEach((k, v) -> result.add(ImmutableEntityReferenceKeyedGroup.builder().key(k).values(v).build()));
        return result;
    }


    public List<Tally<Long>> tallyByCapabilityId() {
        return dao.tallyByCapabilityId();
    }


    public int[] addCapabilitiesToApp(Long appId, List<Long> capabilityIds) {
        return dao.addCapabilitiesToApp(appId, capabilityIds);
    }


    public int[] removeCapabilitiesFromApp(long appId, List<Long> capabilityIds) {
        return dao.removeCapabilitiesFromApp(appId, capabilityIds);
    }


    public int setIsPrimary(long id, long capabilityId, boolean isPrimary) {
        return dao.setIsPrimary(id, capabilityId, isPrimary);
    }


    public List<ApplicationCapability> findByCapabilityIds(List<Long> capIds) {
        return dao.findByCapabilityIds(capIds);

    }

    public List<ApplicationCapability> findApplicationCapabilitiesForAppIds(Long[] ids) {
        return dao.findApplicationCapabilitiesForAppIds(ids);
    }
}
