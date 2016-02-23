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

import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class CapabilityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "capability");

    private final CapabilityService service;


    @Autowired
    public CapabilityEndpoint(CapabilityService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);
        String findByAppIdsPath = mkPath(BASE_URL, "apps");
        String findByIdsPath = mkPath(BASE_URL, "ids");
        String searchPath = mkPath(BASE_URL, "search", ":query");


        ListRoute<Capability> findAllRoute = (request, response) -> service.findAll();
        ListRoute<Capability> findByAppIdsRoute = (request, response) -> service.findByAppIds(readBody(request, Long[].class));
        ListRoute<Capability> findByIdsRoute = (request, response) -> service.findByIds(readBody(request, Long[].class));
        ListRoute<Capability> searchRoute = (request, response) -> service.search(request.params("query"));


        getForList(findAllPath, findAllRoute);
        postForList(findByAppIdsPath, findByAppIdsRoute);
        postForList(findByIdsPath, findByIdsRoute);
        getForList(searchPath, searchRoute);
    }

}
