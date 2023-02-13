package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLegalEntityRelationshipResolutionError.class)
@JsonDeserialize(as = ImmutableLegalEntityRelationshipResolutionError.class)
public abstract class LegalEntityRelationshipResolutionError {


    public abstract LegalEntityResolutionErrorCode errorCode();

    public abstract String errorMessage();

}
