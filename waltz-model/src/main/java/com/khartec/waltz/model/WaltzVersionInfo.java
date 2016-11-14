package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Created by dwatkins on 14/11/2016.
 */
@Value.Immutable
//@JsonSerialize(as = ImmutableWaltzVersionInfo.class)
public abstract class WaltzVersionInfo {

    public abstract String pomVersion();
    public abstract String timestamp();
    public abstract String revision();

}
