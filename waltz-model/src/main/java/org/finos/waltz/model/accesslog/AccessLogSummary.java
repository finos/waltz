package org.finos.waltz.model.accesslog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableAccessLogSummary.class)
@JsonDeserialize(as = ImmutableAccessLogSummary.class)
public abstract class AccessLogSummary {

    @Nullable
    public abstract Integer year();
    @Nullable
    public abstract Integer week();
    @Nullable
    public abstract Integer month();

    @Nullable
    public abstract Long sum();

    @Nullable
    public abstract Long counts();

    @Nullable
    public abstract String state();

    @Nullable
    @JsonProperty
    public abstract Map<String, Object> params();

    @Nullable
    public abstract String userId();

    @Nullable
    public abstract LocalDateTime createdAt();
}
