package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationWidgetDatum.class)
public abstract class AttestationWidgetDatum implements CellExternalIdProvider {

    public abstract Set<AttestationEntry> attestations();

}
