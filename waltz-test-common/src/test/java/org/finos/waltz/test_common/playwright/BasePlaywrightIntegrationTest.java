package org.finos.waltz.test_common.playwright;

import org.finos.waltz.service.DIConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DIConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class BasePlaywrightIntegrationTest extends BasePlaywrightTest {


}
