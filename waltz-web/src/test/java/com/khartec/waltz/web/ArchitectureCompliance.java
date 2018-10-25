package com.khartec.waltz.web;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.Ignore;
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
