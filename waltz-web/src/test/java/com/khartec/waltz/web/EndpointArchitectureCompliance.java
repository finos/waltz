/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.web;

import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.extracts.BaseDataExtractor;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class EndpointArchitectureCompliance extends BaseArchitectureComplianceCheck {

    @Test
    public void endpointsNeedMarkerInterface() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Endpoint")
                .should()
                .implement(Endpoint.class);
        rule.check(waltzOnlyClasses);
    }


    @Test
    public void endpointsNeedServiceAnnotation() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .dontHaveSimpleName("StaticResourcesEndpoint")
                .and()
                .haveNameMatching(".*Endpoint")
                .should()
                .beAnnotatedWith(Service.class);
        rule.check(waltzOnlyClasses);
    }


    @Test
    public void extractorsNeedServiceAnnotation() {
        ArchRule rule = classes().that()
                .areAssignableTo(BaseDataExtractor.class)
                .and()
                .dontHaveSimpleName("BaseDataExtractor")
                .should()
                .haveNameMatching(".*Extractor")
                .andShould()
                .beAnnotatedWith(Service.class);
        rule.check(waltzOnlyClasses);
    }


    @Test
    public void extractorsExtendBaseExtractor() {
        ArchRule rule = classes().that()
                .haveNameMatching(".*Extractor")
                .and()
                .dontHaveSimpleName("BaseDataExtractor")
                .should()
                .beAssignableTo(BaseDataExtractor.class);
        rule.check(waltzOnlyClasses);
    }

}
