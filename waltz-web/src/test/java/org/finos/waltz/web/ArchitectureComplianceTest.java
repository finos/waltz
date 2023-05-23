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

import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.Test;


import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureComplianceTest extends BaseArchitectureComplianceTest {

    @Test
    public void noJavaUtilLogging() {
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
                .because("slf4j/logback should be used instead of java.util logger")
                .check(waltzOnlyClasses);

    }


    @Test
    public void noGenericExceptions() {
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
                .check(waltzOnlyClasses);
    }


    @Test
    public void ensureLayersAreRespected() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Web").definedBy("..web..")
                .layer("Endpoints").definedBy("..endpoints.api..")
                .layer("Extractors").definedBy("..endpoints.extracts..")
                .layer("Services").definedBy("..service..")
                .layer("Data").definedBy("..data..")
                .whereLayer("Endpoints").mayOnlyBeAccessedByLayers("Web")
                .whereLayer("Data").mayOnlyBeAccessedByLayers("Services", "Extractors")
                .whereLayer("Services").mayOnlyBeAccessedByLayers("Web")
                .check(waltzOnlyClasses);
    }



}
