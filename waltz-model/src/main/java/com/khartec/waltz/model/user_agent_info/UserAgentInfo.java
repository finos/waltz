package com.khartec.waltz.model.user_agent_info;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.UserNameProvider;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableUserAgentInfo.class)
@JsonDeserialize(as = ImmutableUserAgentInfo.class)
public abstract class UserAgentInfo implements UserNameProvider {

    public abstract String userAgent();
    public abstract String resolution();
    public abstract String operatingSystem();
    public abstract String ipAddress();
    public abstract LocalDateTime loginTimestamp();

}
