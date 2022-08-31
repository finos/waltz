package org.finos.waltz.jobs.tools.survey.config;

import org.immutables.value.Value;

@Value.Immutable
public abstract class TargetSectionConfig {

    public abstract String name();
    public abstract String extIdPrefix();

    public static TargetSectionConfig mkTargetSection(String extId, String name) {
        return ImmutableTargetSectionConfig
                .builder()
                .name(name)
                .extIdPrefix(extId)
                .build();
    }

}
