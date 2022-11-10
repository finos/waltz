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

package org.finos.waltz.web;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class DaoArchitectureComplianceTest extends BaseArchitectureComplianceTest {


    @Test
    public void daosNeedRepositoryAnnotation() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .doNotHaveModifier(ABSTRACT)
                .and()
                .resideInAPackage("..finos..")
                .and()
                .haveNameMatching(".*Dao")
                .should()
                .beAnnotatedWith(Repository.class);
        rule.check(waltzAndJavaUtilClasses);
    }


    @Test
    public void methodsPrefixedFindShouldReturnCollections() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Dao")
                .should(haveFindMethodsThatReturnCollectionsOrMapsOrOptionals);

        rule.check(waltzAndJavaUtilClasses);
    }

}
