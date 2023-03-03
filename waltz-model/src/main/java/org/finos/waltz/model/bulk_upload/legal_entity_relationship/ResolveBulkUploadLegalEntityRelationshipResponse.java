package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableResolveBulkUploadLegalEntityRelationshipParameters.class)
@JsonDeserialize(as = ImmutableResolveBulkUploadLegalEntityRelationshipParameters.class)
public abstract class ResolveBulkUploadLegalEntityRelationshipResponse {

    public abstract BulkUploadMode uploadMode();

    public abstract Set<ResolvedUploadRow> rows();

    public abstract Set<ResolvedAssessmentHeader> assessmentHeaders();

}
