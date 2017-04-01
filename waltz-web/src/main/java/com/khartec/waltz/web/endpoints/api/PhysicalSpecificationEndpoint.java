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

import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalSpecificationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-specification");

    private final PhysicalSpecificationService specificationService;
    private final UserRoleService userRoleService;


    @Autowired
    public PhysicalSpecificationEndpoint(PhysicalSpecificationService specificationService, UserRoleService userRoleService) {
        checkNotNull(specificationService, "specificationService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.specificationService = specificationService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByProducerAppPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id",
                "produces");

        String findByConsumerAppIdPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id",
                "consumes");

        String findByAppPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id");

        String findBySelectorPath = mkPath(
                BASE_URL,
                "selector");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        String deletePath = mkPath(BASE_URL,
                ":id");

        ListRoute<PhysicalSpecification> findByProducerAppRoute =
                (request, response) -> specificationService.findByProducer(getEntityReference(request));

        ListRoute<PhysicalSpecification> findByConsumerAppIdRoute =
                (request, response) -> specificationService.findByConsumer(getEntityReference(request));

        ListRoute<PhysicalSpecification> findBySelectorRoute =
                (request, response) -> specificationService.findBySelector(readIdSelectionOptionsFromBody(request));


        ListRoute<PhysicalSpecification> findByAppRoute =
                (request, response) -> specificationService.findByEntityReference(getEntityReference(request));

        DatumRoute<PhysicalSpecification> getByIdRoute =
                (request, response) -> specificationService.getById(getId(request));

        getForList(findByProducerAppPath, findByProducerAppRoute);
        getForList(findByConsumerAppIdPath, findByConsumerAppIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);

        getForList(findByAppPath, findByAppRoute);
        getForDatum(getByIdPath, getByIdRoute);

        deleteForDatum(deletePath, this::deleteSpecification);
    }


    private CommandResponse<PhysicalSpecificationDeleteCommand> deleteSpecification(Request request, Response response) {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        long specId = getId(request);
        String username = getUsername(request);

        ImmutablePhysicalSpecificationDeleteCommand deleteCommand = ImmutablePhysicalSpecificationDeleteCommand.builder()
                .specificationId(specId)
                .build();

        return specificationService.delete(deleteCommand, username);
    }
}
