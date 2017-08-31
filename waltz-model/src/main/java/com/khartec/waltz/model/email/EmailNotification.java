package com.khartec.waltz.model.email;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;


@Value.Immutable
@JsonSerialize(as = ImmutableEmailNotification.class)
@JsonDeserialize(as = ImmutableEmailNotification.class)
public abstract class EmailNotification {

    public abstract String body();
    public abstract String subject();
    public abstract List<String> recipients();
}
