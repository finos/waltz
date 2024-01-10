package org.finos.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationsView.class)
public interface ApplicationsView {

    Set<Application> applications();

    AssessmentsView primaryAssessments();

    MeasurableRatingsView primaryRatings();

}
