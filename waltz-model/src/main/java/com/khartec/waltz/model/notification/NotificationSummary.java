package com.khartec.waltz.model.notification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableNotificationSummary.class)
@JsonDeserialize(as = ImmutableNotificationSummary.class)
public abstract class NotificationSummary {

    public abstract EntityKind kind();
    public abstract int count();
}
