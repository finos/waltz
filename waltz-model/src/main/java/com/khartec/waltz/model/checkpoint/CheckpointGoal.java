package com.khartec.waltz.model.checkpoint;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCheckpointGoal.class)
@JsonDeserialize(as = ImmutableCheckpointGoal.class)
public abstract class CheckpointGoal {

    public abstract Checkpoint checkpoint();
    public abstract double value();
    public abstract GoalType goalType();

}
