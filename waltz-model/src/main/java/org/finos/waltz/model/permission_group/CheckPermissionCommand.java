package org.finos.waltz.model.permission_group;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CheckPermissionCommand implements Command {

    public abstract String user();

    public abstract EntityReference parentEntityRef();

    public abstract Operation operation();

    public abstract EntityKind subjectKind();

    @Nullable
    public abstract EntityKind qualifierKind();

    @Nullable
    public abstract Long qualifierId();
}
