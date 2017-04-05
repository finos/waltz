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

import com.khartec.waltz.model.flow_diagram.FlowDiagramEntity;
import com.khartec.waltz.service.flow_diagram.FlowDiagramEntityService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class FlowDiagramEntityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "flow-diagram-entity");
    private final FlowDiagramEntityService flowDiagramEntityService;


    @Autowired
    public FlowDiagramEntityEndpoint(FlowDiagramEntityService flowDiagramEntityService) {
        checkNotNull(flowDiagramEntityService, "flowDiagramEntityService cannot be null");
        this.flowDiagramEntityService = flowDiagramEntityService;
    }


    @Override
    public void register() {
        String findByDiagramIdPath = mkPath(BASE_URL, "id", ":id");
        String findByEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");

        ListRoute<FlowDiagramEntity> findByDiagramIdRoute = (req, res)
                -> flowDiagramEntityService.findByDiagramId(getId(req));

        ListRoute<FlowDiagramEntity> findByEntityRoute = (req, res)
                -> flowDiagramEntityService.findByEntityReference(getEntityReference(req));

        getForList(findByDiagramIdPath, findByDiagramIdRoute);
        getForList(findByEntityPath, findByEntityRoute);
    }

}
