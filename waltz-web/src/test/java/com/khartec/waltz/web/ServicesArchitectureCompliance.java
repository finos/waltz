package com.khartec.waltz.web;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ServicesArchitectureCompliance {

    private static JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("com.khartec");


    @Test
    public void servicesNeedServiceAnnotation() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Service")
                .should()
                .beAnnotatedWith(Service.class);
        rule.check(importedClasses);
    }

}
