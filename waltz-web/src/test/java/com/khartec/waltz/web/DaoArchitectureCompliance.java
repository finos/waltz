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

public class DaoArchitectureCompliance extends BaseArchitectureComplianceCheck {


    @Test
    public void daosNeedRepositoryAnnotation() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .resideInAPackage("..khartec..")
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
