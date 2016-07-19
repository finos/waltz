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

import com.khartec.waltz.model.entity_statistic.EntityStatisticSummary;
import com.khartec.waltz.model.entity_statistic.EntityStatisticValue;
import com.khartec.waltz.service.entity_statistic.EntityStatisticService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readOptionsFromBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class EntityStatisticEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-statistic");

    private final EntityStatisticService entityStatisticService;


    @Autowired
    public EntityStatisticEndpoint(EntityStatisticService entityStatisticService) {
        checkNotNull(entityStatisticService, "entityStatisticService cannot be null");
        this.entityStatisticService = entityStatisticService;
    }


    @Override
    public void register() {

        String findStatsSummariesForAppSelectorPath = mkPath(BASE_URL, "summary");
        String findStatValuesForAppSelectorPath = mkPath(BASE_URL, "value", ":statId");

        ListRoute<EntityStatisticSummary> findStatsSummariesForAppSelectorRoute = (request, response)
                -> entityStatisticService.findStatsSummariesForAppIdSelector(readOptionsFromBody(request));

        ListRoute<EntityStatisticValue> findStatValuesForAppSelectorRoute = (request, response)
                -> entityStatisticService.getStatisticValuesForAppIdSelector(WebUtilities.getLong(request, "statId"), readOptionsFromBody(request));

        postForList(findStatsSummariesForAppSelectorPath, findStatsSummariesForAppSelectorRoute);
        postForList(findStatValuesForAppSelectorPath, findStatValuesForAppSelectorRoute);
    }

}
