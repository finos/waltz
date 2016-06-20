package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBrowserInfo.class)
@JsonDeserialize(as = ImmutableBrowserInfo.class)
public abstract class BrowserInfo {

    public abstract String operatingSystem();
    public abstract String resolution();

}
