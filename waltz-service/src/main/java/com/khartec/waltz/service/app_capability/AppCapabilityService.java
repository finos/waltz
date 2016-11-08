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

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.app_capability.AppCapabilityDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.IdGroup;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableIdGroup;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.applicationcapability.GroupedApplications;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;


@Deprecated
@Service
public class AppCapabilityService {

    private final AppCapabilityDao dao;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;


    @Autowired
    public AppCapabilityService(AppCapabilityDao appCapabilityDao, ApplicationIdSelectorFactory appIdSelectorFactory) {
        checkNotNull(appCapabilityDao, "dao must not be null");
        Checks.checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");

        this.dao = appCapabilityDao;
        this.appIdSelectorFactory = appIdSelectorFactory;
    }


    public List<ApplicationCapability> findCapabilitiesForApp(long appId) {
        return dao.findCapabilitiesForApp(appId);
    }


    public GroupedApplications findGroupedApplicationsByCapability(long capabilityId) {
        return time(
                "ACS.findGroupedApplicationsByCapability",
                () -> dao.findGroupedApplicationsByCapability(capabilityId));
    }


    public List<ApplicationCapability> findAssociatedApplicationCapabilities(long capabilityId) {
        return time(
                "ACS.findAssociatedApplicationCapabilities",
                () -> dao.findAssociatedApplicationCapabilities(capabilityId));
    }


    public List<IdGroup> findAssociatedCapabilitiesByApplication(long applicationId) {
        List<ApplicationCapability> capabilitiesForApp = findCapabilitiesForApp(applicationId);

        List<Long> existingCapabilityIds = ListUtilities.map(capabilitiesForApp, ac -> ac.capabilityId());

        List<ApplicationCapability> associated = capabilitiesForApp.stream()
                .map(appCapability -> appCapability.capabilityId())
                .flatMap(capabilityId -> findAssociatedApplicationCapabilities(capabilityId).stream())
                .filter(associatedAppCap -> associatedAppCap.applicationId() != applicationId)
                .filter(associatedAppCap -> ! existingCapabilityIds.contains(associatedAppCap.capabilityId()))
                .collect(Collectors.toList());


        // [ (capId, [appId]) ]
        Map<Long, Collection<Long>> associatedCapabilitiesToApps = MapUtilities.groupBy(
                appCap -> appCap.capabilityId(),
                appCap -> appCap.applicationId(),
                associated);

        return associatedCapabilitiesToApps.entrySet()
                .stream()
                .map(entry -> ImmutableIdGroup.builder()
                    .key(entry.getKey())
                    .values(entry.getValue())
                    .build())
                .collect(Collectors.toList());
    }


    public List<Tally<Long>> tallyByCapabilityId() {

        return time(
                "ACS.tallyByCapabilityId",
                () -> dao.tallyByCapabilityId());
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
        Checks.checkNotNull(capIds, "capIds cannot be null");
        return time(
                "ACS.findByCapabilityIds",
                () -> dao.findByCapabilityIds(capIds));
    }


    public Collection<ApplicationCapability> findByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = appIdSelectorFactory.apply(options);
        return time(
                "ACS.findByAppIdSelector",
                () -> dao.findApplicationCapabilitiesForAppIdSelector(selector));
    }
}
