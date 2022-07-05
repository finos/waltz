package org.finos.waltz.web.endpoints.extracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.finos.waltz.web.json.CellValue;
import org.finos.waltz.web.json.GenericGridJSON;
import org.finos.waltz.web.json.Row;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.MapKeyColumn;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.junit.jupiter.api.Assertions.*;

class JooqQueryTransformerTest {

    private JooqQueryTransformer jooqQueryTransformer = new JooqQueryTransformer();
    private DSLContext testDslContext;
    private ObjectMapper objectMapper;
    private ExtractSpecification extractSpecification;



    @BeforeEach
    public void setUp(){
        testDslContext = createTestDslContext();
        objectMapper = createMapper();
        extractSpecification = createJsonSpecification();
    }


    @Test
    void recordsFoundAreTransformRecordsToJsonModel() {
        GenericGridJSON genericGridJSON =
                jooqQueryTransformer.transformFromQuery(extractSpecification,testDslContext);
        assertEquals("name", genericGridJSON.name());
        assertEquals("id", genericGridJSON.id());
        List<Row> rows  = genericGridJSON.grid().rows();
        assertEquals(1,rows.size());
        List<CellValue> cells = rows.get(0).cells();
        assertEquals(3,cells.size());
    }


    @Test
    void validJsonModelCanBeTransformedToAString() throws Exception{

        SelectJoinStep<Record6<Long, Long, String, String, String, String>>
                query = createDummyQuery();
        GenericGridJSON genericGridJSON =
                jooqQueryTransformer.transformFromQuery(extractSpecification,testDslContext);
        String json = objectMapper.writeValueAsString(genericGridJSON);
        assertNotNull(json);

    }

        private DSLContext createTestDslContext(){
        MockDataProvider provider = context -> {
            DSLContext create = DSL.using(SQLDialect.SQLSERVER2017);
            Result<OrganisationalUnitRecord> result = create.newResult(ORGANISATIONAL_UNIT);
            result.add(create.newRecord(ORGANISATIONAL_UNIT)
                    .with(ORGANISATIONAL_UNIT.ID,1L)
                    .with(ORGANISATIONAL_UNIT.PARENT_ID,0L)
                    .with(ORGANISATIONAL_UNIT.NAME,"org-name")
            );
            return new MockResult[]{
                    new MockResult(1, result)
            };
        };
        Connection connection = new MockConnection(provider);
        return DSL.using(connection, SQLDialect.SQLSERVER2017);
    }

    private SelectJoinStep<Record6<Long, Long, String, String, String, String>> createDummyQuery(){
        return testDslContext
                .select(
                        ORGANISATIONAL_UNIT.ID.as("id"),
                        ORGANISATIONAL_UNIT.PARENT_ID.as("parentId"),
                        ORGANISATIONAL_UNIT.NAME.as("name"),
                        ORGANISATIONAL_UNIT.DESCRIPTION.as("description"),
                        ORGANISATIONAL_UNIT.EXTERNAL_ID.as("externalId"),
                        ORGANISATIONAL_UNIT.PROVENANCE.as("provenance"))
                .from(ORGANISATIONAL_UNIT);
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    private ExtractSpecification createJsonSpecification(){
        SelectJoinStep<Record6<Long, Long, String, String, String, String>>
                query = createDummyQuery();
        return ImmutableExtractSpecification.builder()
                .qry(query)
                .id("id")
                .outputName("name")
                .extractFormat(ExtractFormat.JSON)
                .build();

    }
}