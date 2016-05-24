package com.khartec.waltz.model.system.job_log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableJobLog.class)
@JsonDeserialize(as = ImmutableJobLog.class)
public abstract class JobLog implements
        NameProvider,
        DescriptionProvider {

    public abstract JobStatus status();
    public abstract EntityKind entityKind();
    public abstract LocalDateTime start();
    public abstract Optional<LocalDateTime> end();


}
