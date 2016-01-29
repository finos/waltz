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

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.*;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.applicationcapability.GroupedApplications;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.user.User;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.action.UpdateAppCapabilitiesAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.*;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.post;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;
import static java.lang.String.format;
import static spark.Spark.delete;


@Service
public class AppCapabilityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "app-capability");

    private static final Logger LOG = LoggerFactory.getLogger(AppCapabilityEndpoint.class);


    private final AppCapabilityService appCapabilityDao;
    private final ChangeLogService changeLogDao;


    @Autowired
    public AppCapabilityEndpoint(AppCapabilityService appCapabilityService, ChangeLogService changeLogDao) {
        checkNotNull(appCapabilityService, "appCapabilityService must not be null");
        checkNotNull(changeLogDao, "changeLogDao must not be null");

        this.changeLogDao = changeLogDao;
        this.appCapabilityDao = appCapabilityService;
    }


    @Override
    public void register() {
        DatumRoute<GroupedApplications> findGroupedAppsForCapability  = (request, response)
                -> appCapabilityDao.findGroupedApplicationsByCapability(getLong(request, "capabilityId"));

        ListRoute<ApplicationCapability> findCapabilitiesForApp = (request, response)
                -> appCapabilityDao.findCapabilitiesForApp(getLong(request, "applicationId"));

        ListRoute<Tally<Long>> tallyByCapability = (request, response)
                -> appCapabilityDao.tallyByCapabilityId();

        ListRoute<ApplicationCapability> findAssociatedAppCapabilities  = (request, response)
                -> appCapabilityDao.findAssociatedApplicationCapabilities(getLong(request, "capabilityId"));

        ListRoute<EntityReferenceKeyedGroup> findAssociatedCapabilitiesByApplication =
                (request, response) -> appCapabilityDao.findAssociatedCapabilitiesByApplication(getId(request));

        ListRoute<ApplicationCapability> findAppCapabilitiesForAppIds  = (request, response)
                -> appCapabilityDao.findApplicationCapabilitiesForAppIds(readBody(request, Long[].class));

        EndpointUtilities.getForDatum(mkPath(BASE_URL, "capability", ":capabilityId"), findGroupedAppsForCapability);
        getForList(mkPath(BASE_URL, "application", ":applicationId"), findCapabilitiesForApp);
        getForList(mkPath(BASE_URL, "count-by", "capability"), tallyByCapability);
        getForList(mkPath(BASE_URL, "capability", ":capabilityId", "associated"), findAssociatedAppCapabilities);
        getForList(mkPath(BASE_URL, "application", ":id", "associated"), findAssociatedCapabilitiesByApplication);
        postForList(mkPath(BASE_URL, "apps"), findAppCapabilitiesForAppIds);

        post(mkPath(BASE_URL, "application", ":id"), (request, response) -> {

            UpdateAppCapabilitiesAction action = readBody(request, UpdateAppCapabilitiesAction.class);

            LOG.info("Updating application capabilities: " + action);
            EntityReference appRef = ImmutableEntityReference.builder()
                    .kind(EntityKind.APPLICATION)
                    .id(getId(request))
                    .name(Optional.empty())
                    .build();

            int[] additions = appCapabilityDao.addCapabilitiesToApp(appRef.id(), map(action.additions(), a -> a.id()));
            int[] removals = appCapabilityDao.removeCapabilitiesFromApp(appRef.id(), map(action.removals(), a -> a.id()));

            logChanges(action, appRef, getUser(request));

            return additions.length + removals.length;
        });

        delete(mkPath(BASE_URL, "application", ":id", ":capabilityId"), (req, res) -> {
            long id = getId(req);

            List<Long> capabilityIds = newArrayList(getLong(req, "capabilityId"));

            LOG.info("Removing application capabilities: " + capabilityIds + " for application: " + id);
            return appCapabilityDao.removeCapabilitiesFromApp(id, capabilityIds)[0];
        });


        post(mkPath(BASE_URL, "application", ":id", ":capabilityId"), (req, res) -> {
            long id = getId(req);
            List<Long> capabilityIds = newArrayList(getLong(req, "capabilityId"));
            LOG.info("Adding application capabilities: " + capabilityIds + " for application: " + id);
            return appCapabilityDao.addCapabilitiesToApp(id, capabilityIds)[0];
        });

        post(mkPath(BASE_URL, "application", ":id", ":capabilityId", "primary"), (req, res) -> {
            long appId = getId(req);
            long capabilityId = getLong(req, "capabilityId");
            boolean isPrimary = WebUtilities.readBody(req, Boolean.class);

            LOG.info("Setting application capability: " + capabilityId + " primary flag to:  " + isPrimary + " for application: " + appId);

            return appCapabilityDao.setIsPrimary(appId, capabilityId, isPrimary);
        });
    }


    private void logChanges(UpdateAppCapabilitiesAction action, EntityReference appRef, User user) {
        List<String> additionMessages = map(
                action.additions(),
                ref -> format("Added capability [%s]", ref.name().orElse("[unknown]")));

        List<String> removalMessages = map(
                action.removals(),
                ref -> format("Removed capability [%s]", ref.name().orElse("[unknown]")));

        List<String> messages = concat(
                additionMessages,
                removalMessages);

        messages.forEach(
                message -> changeLogDao.write(ImmutableChangeLog.builder()
                        .parentReference(appRef)
                        .userId(user.userName())
                        .severity(Severity.INFORMATION)
                        .message(message)
                        .build()));
    }
}

