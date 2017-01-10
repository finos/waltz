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

import com.khartec.waltz.model.trait.Trait;
import com.khartec.waltz.service.trait.TraitService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class TraitEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "trait");
    private final TraitService service;


    @Autowired
    public TraitEndpoint(TraitService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByIdsPath = mkPath(BASE_URL, "id");
        String findByAppDeclarablePath = mkPath(BASE_URL, "application-declarable");

        ListRoute<Trait> findAllRoute = (request, response) -> service.findAll();
        DatumRoute<Trait> getByIdRoute = (request, response) -> service.getById(getId(request));
        ListRoute<Trait> findByIdsRoute = (request, response) -> service.findByIds(readIdsFromBody(request));
        ListRoute<Trait> findByAppDeclarableRoute = (request, response) -> service.findApplicationDeclarableTraits();

        getForList(findAllPath, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForList(findByIdsPath, findByIdsRoute);
        getForList(findByAppDeclarablePath, findByAppDeclarableRoute);
    }

}
