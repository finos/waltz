/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class RatingSchemeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "rating-scheme");
    private final RatingSchemeService ratingSchemeService;

    @Autowired
    public RatingSchemeEndpoint(RatingSchemeService ratingSchemeService) {
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        this.ratingSchemeService = ratingSchemeService;
    }

    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String getByIdPath = mkPath(BASE_URL, "id", ":id");

        getForList(findAllPath, (req, resp) -> ratingSchemeService.findAll());
        getForDatum(getByIdPath, (req, resp) -> ratingSchemeService.getById(getId(req)));
    }
}
