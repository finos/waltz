package org.finos.waltz.model.changelog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableChangeLogComment.class)
@JsonDeserialize(as = ImmutableChangeLogComment.class)
public abstract class ChangeLogComment {

    @Nullable
    public abstract String comment();
}
