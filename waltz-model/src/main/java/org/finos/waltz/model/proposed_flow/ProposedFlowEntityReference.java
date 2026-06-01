package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowEntityReference.class)
@JsonDeserialize(as = ImmutableProposedFlowEntityReference.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ProposedFlowEntityReference implements ExternalIdProvider {

    public abstract EntityKind kind();
    public abstract long id();

    @Value.Auxiliary
    public abstract Optional<String> name();

    @Value.Auxiliary
    @Override
    public abstract Optional<String> externalId();

    public static <T extends NameProvider & IdProvider> ProposedFlowEntityReference fromEntity(T entity, EntityKind kind) {
        Long id = entity.id()
                .orElseThrow(() -> new IllegalArgumentException("Cannot create a reference from an entity with an empty id"));
        return mkRef(kind, id, entity.name());
    }

    public static ProposedFlowEntityReference mkRef(EntityKind kind, long id) {
        return mkRef(kind, id, null);
    }

    public static ProposedFlowEntityReference mkRef(EntityKind kind, long id, String name) {
        return mkRef(kind, id, name, null);
    }

    public static ProposedFlowEntityReference mkRef(EntityKind kind, long id, String name, String externalId) {
        return ImmutableProposedFlowEntityReference.builder()
                .kind(kind)
                .id(id)
                .name(Optional.ofNullable(name))
                .externalId(Optional.ofNullable(externalId))
                .build();
    }

    public static ProposedFlowEntityReference mkRef(Map map) {
        checkNotNull(map, "map cannot be null");
        checkTrue(map.containsKey("kind"), "kind does not exist");
        checkTrue(map.containsKey("id"), "id does not exist");
        checkNotNull(map.get("kind"), "kind cannot be null");
        checkNotNull(map.get("id"), "id cannot be null");

        return ProposedFlowEntityReference.mkRef(
                Enum.valueOf(EntityKind.class, map.get("kind").toString()),
                Long.parseLong(map.get("id").toString()),
                (String) map.get("name"));
    }

}
