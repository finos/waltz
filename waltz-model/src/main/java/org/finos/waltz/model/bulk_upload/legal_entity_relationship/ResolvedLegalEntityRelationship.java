package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableResolvedLegalEntityRelationship.class)
@JsonDeserialize(as = ImmutableResolvedLegalEntityRelationship.class)
public abstract class ResolvedLegalEntityRelationship {

    public abstract Optional<EntityReference> applicationReference();

    public abstract Optional<EntityReference> legalEntityReference();

    public abstract Optional<String> comment();

    public abstract Set<LegalEntityRelationshipResolutionError> errors();

    public abstract ResolutionStatus status();

}
