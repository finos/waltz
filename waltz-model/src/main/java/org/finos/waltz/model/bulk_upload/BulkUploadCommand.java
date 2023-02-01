package org.finos.waltz.model.bulk_upload;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUploadCommand.class)
@JsonDeserialize(as = ImmutableBulkUploadCommand.class)
public abstract class BulkUploadCommand implements Command {

    public abstract BulkUploadMode uploadMode();

    @Value.Redacted
    public abstract String inputString();

    public abstract EntityReference targetDomain();

    public abstract EntityKind rowSubjectKind();

    public abstract Optional<EntityReference> rowSubjectQualifier();
}
