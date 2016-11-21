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


import com.khartec.waltz.model.IdGroup;
import com.khartec.waltz.model.application_capability.ApplicationCapability;
import com.khartec.waltz.model.application_capability.GroupedApplications;
import com.khartec.waltz.model.application_capability.SaveAppCapabilityCommand;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Route;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static spark.Spark.delete;


@Deprecated
@Service
public class AppCapabilityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "app-capability");

    private static final Logger LOG = LoggerFactory.getLogger(AppCapabilityEndpoint.class);


    private final AppCapabilityService appCapabilityService;
    private final UserRoleService userRoleService;


    @Autowired
    public AppCapabilityEndpoint(AppCapabilityService appCapabilityService,
                                 UserRoleService userRoleService) {
        checkNotNull(appCapabilityService, "appCapabilityService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.appCapabilityService = appCapabilityService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        DatumRoute<GroupedApplications> findGroupedAppsForCapabilityRoute  = (request, response)
                -> appCapabilityService.findGroupedApplicationsByCapability(getLong(request, "capabilityId"));

        ListRoute<ApplicationCapability> findCapabilitiesForAppRoute = (request, response)
                -> appCapabilityService.findCapabilitiesForApp(getLong(request, "applicationId"));

        ListRoute<Tally<Long>> tallyByCapabilityRoute = (request, response)
                -> appCapabilityService.tallyByCapabilityId();

        ListRoute<ApplicationCapability> findAssociatedAppCapabilitiesRoute  = (request, response)
                -> appCapabilityService.findAssociatedApplicationCapabilities(getLong(request, "capabilityId"));

        ListRoute<IdGroup> findAssociatedCapabilitiesByApplicationRoute =
                (request, response) -> appCapabilityService.findAssociatedCapabilitiesByApplication(getId(request));

        ListRoute<ApplicationCapability> findAppCapabilitiesForAppIdSelectorRoute  = (request, response)
                -> appCapabilityService.findByAppIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<ApplicationCapability> findByCapabilityIdsRoute  = (request, response)
                -> appCapabilityService.findByCapabilityIds(readIdsFromBody(request));

        Route deleteRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.RATING_EDITOR);

            long id = getId(req);
            Long capabilityId = getLong(req, "capabilityId");
            LOG.info("Removing application capabilities: " + capabilityId + " for application: " + id);

            return appCapabilityService.removeCapabilityFromApp(id, capabilityId, getUsername(req));
        };

        DatumRoute<Integer> saveRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.RATING_EDITOR);
            String username = getUsername(req);

            long appId = getId(req);
            SaveAppCapabilityCommand saveCmd = readBody(req, SaveAppCapabilityCommand.class);
            LOG.info("Saving " + saveCmd + " for application: " + appId);

            return appCapabilityService.save(appId, saveCmd, username);
        };

        getForDatum(mkPath(BASE_URL, "capability", ":capabilityId"), findGroupedAppsForCapabilityRoute);
        getForList(mkPath(BASE_URL, "application", ":applicationId"), findCapabilitiesForAppRoute);
        getForList(mkPath(BASE_URL, "count-by", "capability"), tallyByCapabilityRoute);
        getForList(mkPath(BASE_URL, "capability", ":capabilityId", "associated"), findAssociatedAppCapabilitiesRoute);
        getForList(mkPath(BASE_URL, "application", ":id", "associated"), findAssociatedCapabilitiesByApplicationRoute);
        postForList(mkPath(BASE_URL, "selector"), findAppCapabilitiesForAppIdSelectorRoute);
        postForList(mkPath(BASE_URL, "capability"), findByCapabilityIdsRoute);
        delete(mkPath(BASE_URL, "application", ":id", ":capabilityId"), deleteRoute);
        postForDatum(mkPath(BASE_URL, "application", ":id"), saveRoute);
    }



}

