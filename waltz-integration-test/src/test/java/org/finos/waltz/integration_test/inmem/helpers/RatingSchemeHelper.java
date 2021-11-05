package org.finos.waltz.integration_test.inmem.helpers;

import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.Tables.RATING_SCHEME;

@Service
public class RatingSchemeHelper {

    @Autowired
    private DSLContext dsl;

    public long createEmptyRatingScheme(String name) {
        return dsl
            .select(RATING_SCHEME.ID)
            .from(RATING_SCHEME)
            .where(RATING_SCHEME.NAME.eq(name))
            .fetchOptional(RATING_SCHEME.ID)
            .orElseGet(() -> {
                RatingSchemeRecord record = dsl.newRecord(RATING_SCHEME);
                record.setName(name);
                record.setDescription(name);
                record.store();
                return record.getId();
            });
    }


}
