package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReportGridSchema.class, name = ReportGridSchema.TYPE)
})
@JsonPropertyOrder({"apiTypes","type"})
public interface Schema {

    ApiTypes apiTypes();

    String type();


}
