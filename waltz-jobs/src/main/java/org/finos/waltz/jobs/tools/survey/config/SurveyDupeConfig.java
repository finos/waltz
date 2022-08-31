package org.finos.waltz.jobs.tools.survey.config;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public abstract class SurveyDupeConfig {


    public abstract String sourceSurveyExternalId();
    public abstract String targetSurveyName();
    public abstract String targetSurveyExternalId();

    public abstract Set<SectionDupeConfig> sections();

}
