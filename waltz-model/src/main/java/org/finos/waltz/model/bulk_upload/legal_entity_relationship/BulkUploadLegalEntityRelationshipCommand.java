package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUploadLegalEntityRelationshipCommand.class)
@JsonDeserialize(as = ImmutableBulkUploadLegalEntityRelationshipCommand.class)
public abstract class BulkUploadLegalEntityRelationshipCommand implements Command {

    public abstract BulkUploadMode uploadMode();

    public abstract long legalEntityRelationshipKindId();

    @Value.Redacted
    public abstract String inputString();

}
