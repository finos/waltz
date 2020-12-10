/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.web;

import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.extracts.DataExtractor;
import com.khartec.waltz.web.endpoints.extracts.DirectQueryBasedDataExtractor;
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
                .doNotHaveSimpleName("StaticResourcesEndpoint")
                .and()
                .haveNameMatching(".*Endpoint")
                .should()
                .beAnnotatedWith(Service.class);
        rule.check(waltzOnlyClasses);
    }


    @Test
    public void extractorsNeedServiceAnnotation() {
        ArchRule rule = classes().that()
                .areAssignableTo(DataExtractor.class)
                .and()
                .doNotHaveSimpleName("DataExtractor")
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
                .doNotHaveSimpleName("DirectQueryBasedDataExtractor")
                .should()
                .beAssignableTo(DirectQueryBasedDataExtractor.class);
        rule.check(waltzOnlyClasses);
    }

}
