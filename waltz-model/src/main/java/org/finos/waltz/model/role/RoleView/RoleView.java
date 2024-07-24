package org.finos.waltz.model.role.RoleView;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.role.Role;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableRoleView.class)
public abstract class RoleView {

    public abstract Role role();
    public abstract Set<String> users();

}
