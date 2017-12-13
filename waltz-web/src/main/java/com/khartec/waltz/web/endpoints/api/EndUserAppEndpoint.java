/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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


import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.end_user_app.EndUserAppService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class EndUserAppEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "end-user-application");

    private final EndUserAppService endUserAppService;

    @Autowired
    public EndUserAppEndpoint(EndUserAppService endUserAppService) {
        this.endUserAppService = endUserAppService;
    }


    @Override
    public void register() {
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String countByOrgUnitPath = mkPath(BASE_URL, "count-by", "org-unit");

        ListRoute<EndUserApplication> findBySelectorRoute = (request, response)
                -> endUserAppService.findByOrganisationalUnitSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<Tally<Long>> countByOrgUnitRoute = (request, response) -> endUserAppService.countByOrgUnitId();

        getForList(countByOrgUnitPath, countByOrgUnitRoute);

        postForList(findBySelectorPath, findBySelectorRoute);
    }

}
