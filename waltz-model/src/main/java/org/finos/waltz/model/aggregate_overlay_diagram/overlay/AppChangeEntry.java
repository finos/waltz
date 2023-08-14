package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.ChangeDirection;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize(as = ImmutableAppChangeEntry.class)
public abstract class AppChangeEntry {

    public abstract Long appId();
    public abstract ChangeDirection changeDirection();
    public abstract LocalDate date();

}