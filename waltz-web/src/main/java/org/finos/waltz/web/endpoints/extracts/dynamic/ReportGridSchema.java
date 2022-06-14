package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridSchema.class)
@JsonDeserialize(as = ImmutableReportGridSchema.class)
@JsonPropertyOrder({"type","id","name"})
public interface ReportGridSchema extends Schema{

    String TYPE ="http://waltz.intranet.db.com/types/1/schema#id=report-grid";

    @Value.Default
    default String type() {
        return TYPE;
    }
    String id();
    String name();

    Grid grid();
}