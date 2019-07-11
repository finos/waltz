package com.khartec.waltz.jobs.tools.resolvers;

import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.INVOLVEMENT_KIND;

public class InvolvementNameToIdResolver implements Resolver<Long> {

    private final Map<String, Long> map;


    public InvolvementNameToIdResolver(DSLContext dsl) {
        checkNotNull(dsl, "DSL cannot be null");
        map = loadMap(dsl);
    }


    @Override
    public Optional<Long> resolve(String name) {
        return Optional.ofNullable(map.get(normalize(name)));
    }


    private Map<String, Long> loadMap(DSLContext dsl) {
        Field<String> normalizedName = INVOLVEMENT_KIND.NAME.trim().lower();
        Map<String, Long> result = new HashMap<>();
        dsl.select(normalizedName, INVOLVEMENT_KIND.ID)
                .from(INVOLVEMENT_KIND)
                .fetch()
                .forEach(r -> result.put(r.get(normalizedName), r.get(INVOLVEMENT_KIND.ID)));

        return result;
    }
}

