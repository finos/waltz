package org.finos.waltz.integration_test.inmem;

import org.finos.waltz.common.LoggingUtilities;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DIZonkyTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class BaseZonkyIntegrationTest {

    static {
        LoggingUtilities.configureLogging();
    }

}
