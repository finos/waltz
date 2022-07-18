package org.finos.waltz.web.endpoints.extracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.sql.Connection;

import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgUnitExtractorTest {

    private final ObjectMapper objectMapper = createMapper();
    private DSLContext testDslContext;
    private OrgUnitExtractor orgUnitExtractor;

    @Mock
    private Request request;
    @Mock
    private Response response;

    @BeforeEach
    public void setUp(){
        testDslContext = createTestDslContext();
        orgUnitExtractor = new OrgUnitExtractor(testDslContext);
    }


    @Test
    void recordsFoundAreTransformRecordsToJsonModel() throws IOException {
        when(request.queryParams("format"))
                .thenReturn("JSON");
        Object obj =
                orgUnitExtractor.writeExtract("name",createDummyQuery(),request, response);
        assertTrue(obj instanceof String);
        String responseJSON = (String)obj;
        assertTrue(responseJSON.length()>0);
        JsonNode node = objectMapper.readTree(responseJSON);
        JsonNode arrElement = node.get(0);
        assertNotNull(arrElement);
        assertEquals("1", arrElement.get("id").asText());
        assertEquals("0", arrElement.get("parent_id").asText());
        assertEquals("org-name", arrElement.get("name").asText());

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
}