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

import com.khartec.waltz.model.dataflow.RatedDataFlow;
import com.khartec.waltz.service.data_flow.RatedDataFlowService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getLong;
import static com.khartec.waltz.web.WebUtilities.mkPath;


/**
 * A RatedDataFlow is a combination of a data flow and a rating derived
 * from the receivers org-unit authoritative sources
 */
@Deprecated
@Service
public class RatedDataFlowsEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "rated-data-flows");

    private final RatedDataFlowService ratedDataFlowService;


    @Autowired
    public RatedDataFlowsEndpoint(RatedDataFlowService ratedDataFlowService) {
        checkNotNull(ratedDataFlowService, "ratedDataFlowService must not be null");
        this.ratedDataFlowService = ratedDataFlowService;
    }


    @Override
    public void register() {

        String findByOrgUnitPath = mkPath(BASE_URL, "org-unit-tree", ":orgUnitId");

        ListRoute<RatedDataFlow> findByOrgUnitRoute = (request, response) -> {
            long orgUnitId = getLong(request, "orgUnitId");
            return ratedDataFlowService.calculateRatedFlowsForOrgUnitTree(orgUnitId);
        };

        EndpointUtilities.getForList(findByOrgUnitPath, findByOrgUnitRoute);
    }

}
