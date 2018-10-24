package com.khartec.waltz.web;

import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.extracts.BaseDataExtractor;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class EndpointArchitectureCompliance {

    private static JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("com.khartec");


    @Test
    public void endpointsNeedMarkerInterface() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Endpoint")
                .should()
                .implement(Endpoint.class);
        rule.check(importedClasses);
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
        rule.check(importedClasses);
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
        rule.check(importedClasses);
    }


    @Test
    public void extractorsExtendBaseExtractor() {
        ArchRule rule = classes().that()
                .haveNameMatching(".*Extractor")
                .and()
                .dontHaveSimpleName("BaseDataExtractor")
                .should()
                .beAssignableTo(BaseDataExtractor.class);
        rule.check(importedClasses);
    }

}
