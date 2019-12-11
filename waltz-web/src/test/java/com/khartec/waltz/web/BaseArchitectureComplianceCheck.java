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

import com.khartec.waltz.common.SetUtilities;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.BeforeClass;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.LoggingUtilities.configureLogging;

/**
 * It is advisable to add a rule into your logback config to switch
 * logger `com.tngtech` to `WARN` as it is very chatty.
 */
public class BaseArchitectureComplianceCheck {

    static JavaClasses waltzAndJavaUtilClasses = null;
    static JavaClasses waltzOnlyClasses = null;

    @BeforeClass
    public static void beforeClass() {
        configureLogging();
        waltzAndJavaUtilClasses = new ClassFileImporter().importPackages("com.khartec", "java.util");
        waltzOnlyClasses = new ClassFileImporter().importPackages("com.khartec");
    }

    static ArchCondition<JavaClass> haveFindMethodsThatReturnCollectionsOrMapsOrOptionals =
        new ArchCondition<JavaClass>("have 'find*' methods that return Collections, Maps or Optionals")
        {
            Set<Class<?>> validReturnTypes = SetUtilities.asSet(Collection.class, Optional.class, Map.class);

            @Override
            public void check(JavaClass item, ConditionEvents events) {
                item.getMethods()
                        .stream()
                        .filter(m -> m.getName().startsWith("find"))
                        .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                        .forEach(m -> {
                            JavaClass returnType = m.getRawReturnType();
                            if (! any(validReturnTypes, returnType::isAssignableTo)) {
                                String message = String.format(
                                        "Method %s.%s does not return a collection, map or optional. It returns: %s",
                                        item.getName(),
                                        m.getName(),
                                        returnType.getName());
                                events.add(SimpleConditionEvent.violated(item, message));
                            }
                        });
            }
        };

}
