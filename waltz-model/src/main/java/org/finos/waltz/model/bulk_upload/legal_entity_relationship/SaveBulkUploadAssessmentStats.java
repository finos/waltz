package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.BulkChangeStatistics;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableSaveBulkUploadAssessmentStats.class)
@JsonDeserialize(as = ImmutableSaveBulkUploadAssessmentStats.class)
public abstract class SaveBulkUploadAssessmentStats {

    public abstract AssessmentDefinition definition();

    public abstract BulkChangeStatistics assessmentStatistics();

}
