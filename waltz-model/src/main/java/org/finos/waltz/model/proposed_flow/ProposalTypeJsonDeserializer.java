package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ProposalTypeJsonDeserializer extends JsonDeserializer<ProposalType> {
    @Override
    public ProposalType deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        String key;
        if (node.isObject()) {          // {"key":"CREATE"}
            key = node.get("key").asText();
        } else {                        // "CREATE"
            key = node.asText();
        }
        return ProposalType.fromKey(key);
    }
}
