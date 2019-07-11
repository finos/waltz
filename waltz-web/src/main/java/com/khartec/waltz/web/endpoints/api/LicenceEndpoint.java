/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.licence.Licence;
import com.khartec.waltz.service.licence.LicenceService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class LicenceEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LicenceEndpoint.class);
    private static final String BASE_URL = mkPath("api", "licence");

    private final LicenceService service;


    @Autowired
    public LicenceEndpoint(LicenceService service) {
        checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {

        // read
        getForList(mkPath(BASE_URL, "all"), (request, response) -> service.findAll());
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute );
        getForList(mkPath(BASE_URL, "count", "application"), (request, response) -> service.countAppslications());
        postForList(mkPath(BASE_URL, "selector"), this::findBySelectorRoute);
    }


    private Licence getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }


    private  List<Licence> findBySelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions options = WebUtilities.readIdSelectionOptionsFromBody(request);
        return service.findBySelector(options);
    }
}
