package org.finos.waltz.web.endpoints.extracts.dynamic;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


import java.util.HashMap;
import java.util.Map;


@Value.Immutable
@JsonSerialize(as = ImmutableApiTypes.class)
@JsonDeserialize(as = ImmutableApiTypes.class)
public class ApiTypes {

    public final static String KEYCELL = "5";
    public final static String VALCELL = "10";
    private final Map<String, String> apiTypes;


    public ApiTypes() {
        Map<String, String> types = new HashMap<>();
        types.put(KEYCELL, "http://waltz.intranet.db.com/types/1/schema#id=KeyCell");
        types.put(VALCELL, "http://waltz.intranet.db.com/types/1/schema#id=CellValue");
        this.apiTypes = types;
    }

    public Map<String, String> getApiTypes() {
        return apiTypes;
    }
}
