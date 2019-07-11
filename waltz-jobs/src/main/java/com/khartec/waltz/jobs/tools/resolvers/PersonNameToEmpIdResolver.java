package com.khartec.waltz.jobs.tools.resolvers;

import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.PERSON;

public class PersonNameToEmpIdResolver implements Resolver<String> {

    private final Map<String, String> personToEmpId;


    public PersonNameToEmpIdResolver(DSLContext dsl) {
        checkNotNull(dsl, "DSL cannot be null");
        personToEmpId = loadPersonToEmpIdMap(dsl);
    }


    @Override
    public Optional<String> resolve(String name) {
        return Optional.ofNullable(personToEmpId.get(normalize(name)));
    }


    private Map<String, String> loadPersonToEmpIdMap(DSLContext dsl) {
        Field<String> normalizedName = PERSON.DISPLAY_NAME.trim().lower();
        return dsl
                .select(normalizedName, PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.IS_REMOVED.isFalse())
                .fetchMap(normalizedName, PERSON.EMPLOYEE_ID);
    }


}
