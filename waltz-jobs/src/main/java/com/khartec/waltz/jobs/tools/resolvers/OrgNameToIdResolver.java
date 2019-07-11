package com.khartec.waltz.jobs.tools.resolvers;

import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.ORGANISATIONAL_UNIT;

public class OrgNameToIdResolver implements Resolver<Long> {

    private final Map<String, Long> orgToIdMap;


    public OrgNameToIdResolver(DSLContext dsl) {
        checkNotNull(dsl, "DSL cannot be null");
        orgToIdMap = loadMap(dsl);
    }


    @Override
    public Optional<Long> resolve(String name) {
        return Optional.ofNullable(orgToIdMap.get(normalize(name)));
    }


    private Map<String, Long> loadMap(DSLContext dsl) {
        Field<String> normalizedName = ORGANISATIONAL_UNIT.NAME.trim().lower();
        Map<String, Long> result = new HashMap<>();
        dsl.select(normalizedName, ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch()
                .forEach(r -> result.put(r.get(normalizedName), r.get(ORGANISATIONAL_UNIT.ID)));

        return result;
    }
}

