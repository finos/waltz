package com.khartec.waltz.model.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAuthenticationResponse.class)
@JsonDeserialize(as = ImmutableAuthenticationResponse.class)
public abstract class AuthenticationResponse {

    public abstract boolean success();

    @Nullable
    public abstract String waltzUserName();

    @Nullable
    public abstract String errorMessage();
}
