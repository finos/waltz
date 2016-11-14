package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableWaltzVersionInfo.class)
@JsonDeserialize(as = ImmutableWaltzVersionInfo.class)
public abstract class WaltzVersionInfo {

    public abstract String pomVersion();
    public abstract String timestamp();
    public abstract String revision();

}
