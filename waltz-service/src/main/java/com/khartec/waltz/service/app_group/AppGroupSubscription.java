package com.khartec.waltz.service.app_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupMemberRole;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableAppGroupSubscription.class)
@JsonDeserialize(as = ImmutableAppGroupSubscription.class)
public abstract class AppGroupSubscription {

    public abstract AppGroup appGroup();
    public abstract AppGroupMemberRole role();

}
