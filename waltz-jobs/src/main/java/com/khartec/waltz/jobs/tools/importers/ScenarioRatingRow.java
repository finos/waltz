package com.khartec.waltz.jobs.tools.importers;


import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ScenarioRatingRow {

    public abstract String roadmap();
    public abstract String scenario();
    public abstract String column();
    public abstract String row();
    public abstract String assetCode();
    public abstract String rating();

    @Nullable
    public abstract String description();

    public abstract String providedBy();
}
