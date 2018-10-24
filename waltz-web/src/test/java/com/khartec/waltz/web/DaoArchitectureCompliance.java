package com.khartec.waltz.web;

import com.khartec.waltz.common.SetUtilities;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.Test;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class DaoArchitectureCompliance {

    private static JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("com.khartec", "java.util");


    @Test
    public void daosNeedRepositoryAnnotation() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Dao")
                .should()
                .beAnnotatedWith(Repository.class);
        rule.check(importedClasses);
    }


    @Test
    public void methodsPrefixedFindShouldReturnCollections() {
        Set<Class<?>> validReturnTypes = SetUtilities.asSet(Collection.class, Optional.class, Map.class);

        ArchCondition<JavaClass> haveFindMethodsThatReturnCollections = new ArchCondition<JavaClass>("have 'find*' methods that return Collections, Maps or Optionals") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                item.getMethods()
                        .stream()
                        .filter(m -> m.getName().startsWith("find"))
                        .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                        .forEach(m -> {
                            JavaClass returnType = m.getReturnType();
                            if (! any(validReturnTypes, vrt -> returnType.isAssignableTo(vrt))) {
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

        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Dao")
                .should(haveFindMethodsThatReturnCollections);

        rule.check(importedClasses);
    }

}
