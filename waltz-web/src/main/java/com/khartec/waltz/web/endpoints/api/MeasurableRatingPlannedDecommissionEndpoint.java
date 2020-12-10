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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.command.DateFieldChange;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.MEASURABLE;
import static com.khartec.waltz.model.EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class MeasurableRatingPlannedDecommissionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-rating-planned-decommission");


    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final UserRoleService userRoleService;


    @Autowired
    public MeasurableRatingPlannedDecommissionEndpoint(MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                                       UserRoleService userRoleService) {
        checkNotNull(measurableRatingPlannedDecommissionService, "measurableRatingPlannedDecommissionService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForReplacingEntityPath = mkPath(BASE_URL, "replacing-entity", ":kind", ":id");
        String savePath = mkPath(BASE_URL, "entity", ":kind", ":id", "MEASURABLE", ":measurableId");
        String removePath = mkPath(BASE_URL, "id", ":id");

        ListRoute<MeasurableRatingPlannedDecommission> findForEntityRoute = (request, response)
                -> measurableRatingPlannedDecommissionService.findForEntityRef(getEntityReference(request));

        ListRoute<MeasurableRatingPlannedDecommission> findForReplacingEntityRoute = (request, response)
                -> measurableRatingPlannedDecommissionService.findForReplacingEntityRef(getEntityReference(request));

        DatumRoute<MeasurableRatingPlannedDecommission> saveRoute = (request, response) -> {
            EntityReference entityRef = getEntityReference(request);
            long measurableId = getLong(request, "measurableId");

            requireRole(userRoleService, request, measurableRatingPlannedDecommissionService.getRequiredRatingEditRole(mkRef(MEASURABLE, measurableId)));

            return measurableRatingPlannedDecommissionService.save(
                    entityRef,
                    measurableId,
                    readBody(request, DateFieldChange.class),
                    getUsername(request));
        };

        DatumRoute<Boolean> removeRoute = (request, response) -> {
            long decommissionId = getId(request);
            EntityReference entityRef = mkRef(MEASURABLE_RATING_PLANNED_DECOMMISSION, decommissionId);

            requireRole(userRoleService, request,  measurableRatingPlannedDecommissionService.getRequiredRatingEditRole(entityRef));

            return measurableRatingPlannedDecommissionService.remove(decommissionId, getUsername(request));
        };

        getForList(findForEntityPath, findForEntityRoute);
        getForList(findForReplacingEntityPath, findForReplacingEntityRoute);
        postForDatum(savePath, saveRoute);
        deleteForDatum(removePath, removeRoute);

    }
}
