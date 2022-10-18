package org.finos.waltz.jobs.tools.survey.config;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class SectionDupeConfig {

    public abstract String sectionName();
    public abstract List<TargetSectionConfig> targets();

}
