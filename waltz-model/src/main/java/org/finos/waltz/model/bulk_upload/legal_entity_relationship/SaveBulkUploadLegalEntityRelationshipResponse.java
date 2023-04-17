package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.BulkChangeStatistics;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableSaveBulkUploadLegalEntityRelationshipResponse.class)
@JsonDeserialize(as = ImmutableSaveBulkUploadLegalEntityRelationshipResponse.class)
public abstract class SaveBulkUploadLegalEntityRelationshipResponse {

    public abstract BulkChangeStatistics relationshipStats();

    public abstract Set<SaveBulkUploadAssessmentStats> assessmentStats();

}
