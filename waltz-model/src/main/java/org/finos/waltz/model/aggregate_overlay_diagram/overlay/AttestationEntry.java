package org.finos.waltz.model.aggregate_overlay_diagram.overlay;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationEntry.class)
public abstract class AttestationEntry {

    public abstract long appId();

    @Nullable
    public abstract LocalDateTime attestedAt();

    @Nullable
    public abstract String attestedBy();


    public static AttestationEntry mkUnattestedEntry(long appId) {
        return ImmutableAttestationEntry.builder().appId(appId).build();
    }
}

