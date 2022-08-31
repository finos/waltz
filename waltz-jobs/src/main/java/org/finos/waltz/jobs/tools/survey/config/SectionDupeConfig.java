package org.finos.waltz.jobs.tools.survey.config;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public abstract class SectionDupeConfig {

    public abstract String sectionName();
    public abstract Set<TargetSectionConfig> targets();

}
