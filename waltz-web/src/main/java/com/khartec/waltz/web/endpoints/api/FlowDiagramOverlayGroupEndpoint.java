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

import com.khartec.waltz.model.flow_diagram.FlowDiagramOverlayGroup;
import com.khartec.waltz.service.flow_diagram.FlowDiagramOverlayGroupService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class FlowDiagramOverlayGroupEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "flow-diagram-overlay-group");
    private final FlowDiagramOverlayGroupService flowDiagramOverlayGroupService;
    private final UserRoleService userRoleService;


    @Autowired
    public FlowDiagramOverlayGroupEndpoint(FlowDiagramOverlayGroupService flowDiagramOverlayGroupService, UserRoleService userRoleService) {
        checkNotNull(flowDiagramOverlayGroupService, "flowDiagramOverlayGroupService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.flowDiagramOverlayGroupService = flowDiagramOverlayGroupService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {
        String findByDiagramIdPath = mkPath(BASE_URL, "id", ":id");

        ListRoute<FlowDiagramOverlayGroup> findByDiagramIdRoute = (req, res)
                -> flowDiagramOverlayGroupService.findByDiagramId(getId(req));

        getForList(findByDiagramIdPath, findByDiagramIdRoute);
    }

}
