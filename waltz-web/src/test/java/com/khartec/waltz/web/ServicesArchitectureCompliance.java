package com.khartec.waltz.web;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ServicesArchitectureCompliance extends BaseArchitectureComplianceCheck {

    @Test
    public void servicesNeedServiceAnnotation() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Service")
                .should()
                .beAnnotatedWith(Service.class);
        rule.check(waltzOnlyClasses);
    }


    @Test
    public void methodsPrefixedFindShouldReturnCollections() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Service")
                .should(haveFindMethodsThatReturnCollectionsOrMapsOrOptionals);

        rule.check(waltzAndJavaUtilClasses);
    }

}
