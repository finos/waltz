package org.finos.waltz.integration_test.inmem.data;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.InvolvementKind;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JooqUtilities_loadExternalIdToEntityRefMapTest extends BaseInMemoryIntegrationTest {

    private static final InvolvementKind ik = Tables.INVOLVEMENT_KIND;
    @Autowired
    private AppHelper appHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private DSLContext dsl;


    private final String stem = "JooqHelper_extIdMap";


    @Test
    void loadExternalIdToEntityRefMap_withoutQualifier() {
        String extId = mkName(stem, "no_qualifier_ext_id");
        String appName = mkName(stem, "no_qualifier_app");
        appHelper.createNewApp(appName, ouIds.root, extId);

        Map<String, EntityReference> map = JooqUtilities.loadExternalIdToEntityRefMap(dsl, EntityKind.APPLICATION);

        assertTrue(map.containsKey(extId));
        assertEquals(Optional.of(appName), map.get(extId).name());
    }


    @Test
    void testLoadExternalIdToEntityRefMap_withPartialQualifier() {
        String ikExtId = mkName(stem, "ik_ext_id");
        String ikName = mkName(stem, "ik_name");
        long ikId = involvementHelper.mkInvolvementKind(
                ikName,
                ikExtId);

        dsl.update(ik)
                .set(ik.SUBJECT_KIND, EntityKind.LEGAL_ENTITY.name())
                .where(ik.ID.eq(ikId))
                .execute();

        Map<String, EntityReference> map = JooqUtilities.loadExternalIdToEntityRefMap(
                dsl,
                EntityKind.INVOLVEMENT_KIND,
                EntityKind.LEGAL_ENTITY);

        assertFalse(map.isEmpty());
        assertTrue(map.containsKey(ikExtId));
        assertEquals(1, map.size());
        assertEquals(Optional.of(ikName), map.get(ikExtId).name());
    }


    @Test
    void testEmptyMap_withFullQualifier() {
        long categoryId = measurableHelper.createMeasurableCategory(mkName(stem, "mc"));

        Map<String, EntityReference> map = JooqUtilities.loadExternalIdToEntityRefMap(
                dsl,
                EntityKind.MEASURABLE,
                mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId));

        assertTrue(map.isEmpty());

    }

    @Test
    void testNonEmptyMap_withFullQualifier() {
        long categoryId = measurableHelper.createMeasurableCategory(mkName(stem, "mc"));

        String m1ExtId = mkName(stem, "m1_ext_id");
        String m1Name = mkName(stem, "m1_name");
        measurableHelper.createMeasurable(
                m1ExtId,
                m1Name,
                categoryId);

        Map<String, EntityReference> map = JooqUtilities.loadExternalIdToEntityRefMap(
                dsl,
                EntityKind.MEASURABLE,
                mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId));

        assertFalse(map.isEmpty());
        assertTrue(map.containsKey(m1ExtId));
        assertEquals(1, map.size());
        assertEquals(Optional.of(m1Name), map.get(m1ExtId).name());
    }
}