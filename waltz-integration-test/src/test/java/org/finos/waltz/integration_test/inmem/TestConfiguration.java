package org.finos.waltz.integration_test.inmem;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TestConfigurationSelector.class)
public class TestConfiguration {
}
