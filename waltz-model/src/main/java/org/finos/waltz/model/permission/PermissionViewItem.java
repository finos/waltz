package org.finos.waltz.model.permission;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.Operation;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutablePermissionViewItem.class)
public interface PermissionViewItem {
    EntityKind parentKind();

    EntityKind subjectKind();

    @Nullable
    EntityReference qualifier();

    Operation operation();

    EntityReference involvementGroup();

    EntityReference involvementKind();
}
