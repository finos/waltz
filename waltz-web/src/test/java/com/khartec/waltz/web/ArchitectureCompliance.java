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

import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureCompliance extends BaseArchitectureComplianceCheck {

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
