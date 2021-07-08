package com.khartec.waltz.jobs.tools.importers;

import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
public abstract class AssessmentRatingEntry {

    public abstract EntityReference entity();
    public abstract Long ratingId();
    public abstract String description();

}
