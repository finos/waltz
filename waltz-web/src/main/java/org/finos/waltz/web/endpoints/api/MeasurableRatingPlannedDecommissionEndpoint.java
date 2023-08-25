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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.command.DateFieldChange;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionInfo;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.finos.waltz.service.permission.permission_checker.MeasurableRatingPermissionChecker;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static org.finos.waltz.web.WebUtilities.getEntityReference;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.getLong;
import static org.finos.waltz.web.WebUtilities.getUsername;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.endpoints.EndpointUtilities.deleteForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class MeasurableRatingPlannedDecommissionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-rating-planned-decommission");

    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final MeasurableRatingPermissionChecker measurableRatingPermissionChecker;

    @Autowired
    public MeasurableRatingPlannedDecommissionEndpoint(MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                                       MeasurableRatingPermissionChecker measurableRatingPermissionChecker) {
        checkNotNull(measurableRatingPlannedDecommissionService, "measurableRatingPlannedDecommissionService cannot be null");
        checkNotNull(measurableRatingPermissionChecker, "measurableRatingPermissionChecker cannot be null");

        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.measurableRatingPermissionChecker = measurableRatingPermissionChecker;
    }


    @Override
    public void register() {

        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForReplacingEntityPath = mkPath(BASE_URL, "replacing-entity", ":kind", ":id");
        String savePath = mkPath(BASE_URL, "measurable-rating", ":id");
        String removePath = mkPath(BASE_URL, "id", ":id");

        ListRoute<MeasurableRatingPlannedDecommission> findForEntityRoute = (request, response)
                -> measurableRatingPlannedDecommissionService.findForEntityRef(getEntityReference(request));

        ListRoute<MeasurableRatingPlannedDecommissionInfo> findForReplacingEntityRoute = (request, response)
                -> measurableRatingPlannedDecommissionService.findForReplacingEntityRef(getEntityReference(request));

        DatumRoute<MeasurableRatingPlannedDecommission> saveRoute = (request, response) -> {
            long measurableRatingId = getLong(request, "id");

            checkHasPermissionForThisOperation(measurableRatingId, asSet(Operation.ADD, Operation.UPDATE), getUsername(request));

            return measurableRatingPlannedDecommissionService.save(
                    measurableRatingId,
                    readBody(request, DateFieldChange.class),
                    getUsername(request));
        };

        DatumRoute<Boolean> removeRoute = (request, response) -> {
            long decommissionId = getId(request);

            MeasurableRatingPlannedDecommission decomm = measurableRatingPlannedDecommissionService.getById(decommissionId);

            checkHasPermissionForThisOperation(decomm.measurableRatingId(), asSet(Operation.REMOVE), getUsername(request));

            return measurableRatingPlannedDecommissionService.remove(decommissionId, getUsername(request));
        };

        getForList(findForEntityPath, findForEntityRoute);
        getForList(findForReplacingEntityPath, findForReplacingEntityRoute);
        postForDatum(savePath, saveRoute);
        deleteForDatum(removePath, removeRoute);

    }

    private void checkHasPermissionForThisOperation(Long ratingId,
                                                    Set<Operation> operations,
                                                    String username) throws InsufficientPrivelegeException {

        Set<Operation> perms = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(ratingId, username);
        measurableRatingPermissionChecker.verifyAnyPerms(operations, perms, MEASURABLE_RATING_PLANNED_DECOMMISSION, username);
    }
}
