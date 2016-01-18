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

import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceCalculator;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;


/**
 * Calculates the effective auth sources for a given organisational unit, taking into account
 * it's parents settings
 */
@Service
public class AuthoritativeSourceCalculatorEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "authoritative-source-calculator");

    private final AuthoritativeSourceCalculator calculator;
    private final OrganisationalUnitService organisationalUnitService;


    @Autowired
    public AuthoritativeSourceCalculatorEndpoint(AuthoritativeSourceCalculator calculator, OrganisationalUnitService organisationalUnitService) {
        checkNotNull(calculator, "calculator must not be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService must not be null");
        
        this.calculator = calculator;
        this.organisationalUnitService = organisationalUnitService;
    }


    @Override
    public void register() {
        EndpointUtilities.getForDatum(WebUtilities.mkPath(BASE_URL, "org-unit", ":id"), (request, response) -> {
            long orgUnitId = WebUtilities.getId(request);
            Node<OrganisationalUnit, Long> orgUnitNode = organisationalUnitService.loadHierarchy(orgUnitId);
            return calculator.calculateAuthSourcesForOrgUnit(orgUnitNode);
        });
    }



}
