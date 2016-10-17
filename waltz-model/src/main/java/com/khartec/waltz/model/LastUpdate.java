package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.common.DateTimeUtilities;
import org.immutables.value.Value;

import java.time.LocalDateTime;


@Value.Immutable
@JsonSerialize(as = ImmutableLastUpdate.class)
@JsonDeserialize(as = ImmutableLastUpdate.class)
public abstract class LastUpdate {

    @Value.Default
    public LocalDateTime at() {
        return DateTimeUtilities.nowUtc();
    }


    public abstract String by();

    public static LastUpdate mkForUser(String username) {
        return ImmutableLastUpdate.builder()
                .by(username)
                .build();
    }
}
