package org.finos.waltz.integration_test.inmem;

import org.finos.waltz.schema.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class ZonkyDefaultProviderTest extends BaseZonkyIntegrationTest  {

    @Autowired
    private DSLContext dsl;


    @Test
    public void databaseInitialiseTest() {
        int tableCount = dsl
            .select(DSL.count())
            .from(Tables.DATABASECHANGELOG)
            .fetchOne(0, int.class);

        Assertions.assertTrue(tableCount > 0, "No entries in database change log, run liquibase");
    }
}
