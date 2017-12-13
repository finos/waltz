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

import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class ComplexityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "complexity");

    private final ComplexityRatingService service;


    @Autowired
    public ComplexityEndpoint(ComplexityRatingService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String getForAppPath = mkPath(BASE_URL, "application", ":id");
        String findForAppIdSelectorPath = BASE_URL;
        String rebuildPath = mkPath(BASE_URL, "rebuild");

        DatumRoute<ComplexityRating> getForAppRoute = (request, response) -> service.getForApp(getId(request));
        ListRoute<ComplexityRating> findForAppIdSelectorRoute = (request, response) -> service.findForAppIdSelector(readIdSelectionOptionsFromBody(request));
        DatumRoute<Integer> rebuildRoute = (request, response) -> service.rebuild();

        getForDatum(getForAppPath, getForAppRoute);
        postForList(findForAppIdSelectorPath, findForAppIdSelectorRoute);
        getForDatum(rebuildPath, rebuildRoute);
    }
}
