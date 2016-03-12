package com.khartec.waltz.model.app_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAppGroupDetail.class)
@JsonDeserialize(as = ImmutableAppGroupDetail.class)
public abstract class AppGroupDetail {

    public abstract AppGroup appGroup();
    public abstract List<AppGroupMember> members();
    public abstract List<EntityReference> applications();
}
