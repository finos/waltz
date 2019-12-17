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
