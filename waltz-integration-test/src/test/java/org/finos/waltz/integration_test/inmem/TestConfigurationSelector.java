package org.finos.waltz.integration_test.inmem;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

public class TestConfigurationSelector implements DeferredImportSelector, EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        boolean zonky = env.matchesProfiles("waltz-zonky");
        if(zonky) {
            return new String[] {"org.finos.waltz.integration_test.zonky.DIZonkyTestConfiguration"};
        } else {
            return new String[] {"org.finos.waltz.integration_test.inmem.DIInMemoryTestConfiguration"};
        }
    }
}
