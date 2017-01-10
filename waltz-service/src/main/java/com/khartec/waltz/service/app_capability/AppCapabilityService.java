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

package com.khartec.waltz.service.app_capability;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.app_capability.AppCapabilityDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application_capability.ApplicationCapability;
import com.khartec.waltz.model.application_capability.GroupedApplications;
import com.khartec.waltz.model.application_capability.ImmutableApplicationCapability;
import com.khartec.waltz.model.application_capability.SaveAppCapabilityCommand;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.changelog.ChangeLogService;
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

    private final AppCapabilityDao appCapabilityDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final CapabilityDao capabilityDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public AppCapabilityService(AppCapabilityDao appCapabilityDao,
                                ApplicationIdSelectorFactory appIdSelectorFactory,
                                CapabilityDao capabilityDao,
                                ChangeLogService changeLogService) {
        checkNotNull(appCapabilityDao, "appCapabilityDao must not be null");
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(capabilityDao, "capabilityDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appCapabilityDao = appCapabilityDao;
        this.appIdSelectorFactory = appIdSelectorFactory;
        this.capabilityDao = capabilityDao;
        this.changeLogService = changeLogService;
    }


    public List<ApplicationCapability> findCapabilitiesForApp(long appId) {
        return appCapabilityDao.findCapabilitiesForApp(appId);
    }


    public GroupedApplications findGroupedApplicationsByCapability(long capabilityId) {
        return time(
                "ACS.findGroupedApplicationsByCapability",
                () -> appCapabilityDao.findGroupedApplicationsByCapability(capabilityId));
    }


    public List<ApplicationCapability> findAssociatedApplicationCapabilities(long capabilityId) {
        return time(
                "ACS.findAssociatedApplicationCapabilities",
                () -> appCapabilityDao.findAssociatedApplicationCapabilities(capabilityId));
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
                () -> appCapabilityDao.tallyByCapabilityId());
    }



    public int removeCapabilityFromApp(long appId,
                                         Long capabilityId,
                                         String username) {
        Capability capability = capabilityDao.getById(capabilityId);

        changeLogService.write(ImmutableChangeLog.builder()
                .message(String.format("Removed capability: %s",
                        capability == null
                            ? capabilityId.toString()
                            : capability.name()))
                .parentReference(EntityReference.mkRef(EntityKind.APPLICATION, appId))
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.CAPABILITY)
                .operation(Operation.REMOVE)
                .build());

        return appCapabilityDao.removeCapabilityFromApp(appId, capabilityId);
    }


    public List<ApplicationCapability> findByCapabilityIds(List<Long> capIds) {
        Checks.checkNotNull(capIds, "capIds cannot be null");
        return time(
                "ACS.findByCapabilityIds",
                () -> appCapabilityDao.findByCapabilityIds(capIds));
    }


    public Collection<ApplicationCapability> findByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = appIdSelectorFactory.apply(options);
        return time(
                "ACS.findByAppIdSelector",
                () -> appCapabilityDao.findApplicationCapabilitiesForAppIdSelector(selector));
    }

    public Integer save(long appId, SaveAppCapabilityCommand saveCmd, String username) {
        ImmutableApplicationCapability applicationCapability = ImmutableApplicationCapability.builder()
                .applicationId(appId)
                .capabilityId(saveCmd.capabilityId())
                .rating(saveCmd.rating())
                .description(saveCmd.description())
                .isPrimary(false)
                .lastUpdatedBy(username)
                .provenance("waltz")
                .build();

        Capability capability = capabilityDao.getById(saveCmd.capabilityId());
        checkNotNull(capability, "associated capability cannot be null");

        changeLogService.write(ImmutableChangeLog.builder()
                .message(String.format("%s capability: %s, rating %s",
                        saveCmd.isNew()
                                ? "Added"
                                : "Updated",
                        capability.name(),
                        saveCmd.rating()))
                .parentReference(EntityReference.mkRef(EntityKind.APPLICATION, appId))
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.CAPABILITY)
                .operation(saveCmd.isNew()
                            ? Operation.ADD
                            : Operation.UPDATE)
                .build());

        if (saveCmd.isNew()) {
            return appCapabilityDao.insert(applicationCapability);
        } else {
            return appCapabilityDao.update(applicationCapability);
        }
    }
}
