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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.measurable_relationship.ImmutableMeasurableRelationship;
import com.khartec.waltz.model.measurable_relationship.MeasurableRelationship;
import com.khartec.waltz.model.measurable_relationship.MeasurableRelationshipKind;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.measurable_relationship.MeasurableRelationshipService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class MeasurableRelationshipEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableRelationshipEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "measurable-relationship");

    private final MeasurableRelationshipService measurableRelationshipService;
    private final UserRoleService userRoleService;


    @Autowired
    public MeasurableRelationshipEndpoint(MeasurableRelationshipService measurableRelationshipService, UserRoleService userRoleService) {
        checkNotNull(measurableRelationshipService, "measurableRelationshipService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.measurableRelationshipService = measurableRelationshipService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findForMeasurablePath = mkPath(BASE_URL, "measurable", ":id");
        String removeRelationshipPath = mkPath(BASE_URL, ":measurableA", ":measurableB");
        String saveRelationshipPath = mkPath(BASE_URL, ":measurableA", ":measurableB", ":kind");

        ListRoute<MeasurableRelationship> findForMeasurableRoute = (request, response)
                -> measurableRelationshipService.findForMeasurable(getId(request));

        DatumRoute<Integer> removeRelationshipRoute = (request, response) ->{
            requireRole(userRoleService, request, Role.CAPABILITY_EDITOR);
            return measurableRelationshipService.remove(
                    getLong(request, "measurableA"),
                    getLong(request, "measurableB"));
        };


        DatumRoute<Boolean> saveRelationshipRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.CAPABILITY_EDITOR);
            MeasurableRelationshipKind kind = readEnum(
                    request,
                    "kind",
                    MeasurableRelationshipKind.class,
                    (s) -> MeasurableRelationshipKind.STRONGLY_RELATES_TO);
            MeasurableRelationship measurableRelationship = ImmutableMeasurableRelationship.builder()
                    .measurableA(getLong(request, "measurableA"))
                    .measurableB(getLong(request, "measurableB"))
                    .relationshipKind(kind)
                    .description(request.body())
                    .provenance("waltz")
                    .lastUpdatedBy(getUsername(request))
                    .build();
            return measurableRelationshipService.save(measurableRelationship);
        };


        getForList(findForMeasurablePath, findForMeasurableRoute);
        deleteForDatum(removeRelationshipPath, removeRelationshipRoute);
        postForDatum(saveRelationshipPath, saveRelationshipRoute);
    }

}
