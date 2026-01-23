package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.architecture_required_change.ArchitectureRequiredChange;
import org.finos.waltz.model.architecture_required_change.ImmutableArchitectureRequiredChange;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.change_initiative.ChangeInitiativeKind;
import org.finos.waltz.model.change_initiative.ImmutableChangeInitiative;
import org.finos.waltz.service.architecture_required_change.ArchitectureRequiredChangeService;
import org.finos.waltz.service.change_initiative.ChangeInitiativeService;
import org.finos.waltz.test_common.helpers.ArchitectureRequiredChangeHelper;
import org.finos.waltz.test_common.helpers.ChangeInitiativeHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.schema.Tables.ARCHITECTURE_REQUIRED_CHANGE;
import static org.finos.waltz.schema.Tables.CHANGE_INITIATIVE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class ArchitectureRequiredChangeServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    ChangeInitiativeService ciService;

    @Autowired
    ArchitectureRequiredChangeService arcService;

    @Autowired
    ChangeInitiativeHelper ciHelper;

    @Autowired
    ArchitectureRequiredChangeHelper arcHelper;

    @Autowired
    private final DSLContext dsl = getDsl();

    @Autowired
    private ChangeInitiativeService changeInitiativeService;

    @BeforeEach
    public void setup() {
        System.out.println("Setting up ArchitectureRequiredChangeServiceTest");
        dsl.deleteFrom(CHANGE_INITIATIVE).execute();
        dsl.deleteFrom(ARCHITECTURE_REQUIRED_CHANGE).execute();
        ChangeInitiative ci1 = mkCi(1L, "ci1", "ci1", ChangeInitiativeKind.INITIATIVE, null, ouIds.a);
        ChangeInitiative ci2 = mkCi(2L, "ci2", "ci2", ChangeInitiativeKind.PROGRAMME, ci1.id().get(), ouIds.a);
        mkCi(3L, "ci3", "ci3", ChangeInitiativeKind.PROJECT, ci2.id().get(), ouIds.b);

        createArc(1L, "arc1", "arc1", null, ci1.entityReference().id(), EntityKind.CHANGE_INITIATIVE);
        createArc(2L, "arc2", "arc2", null, ci1.entityReference().id(), EntityKind.CHANGE_INITIATIVE);
        createArc(3L, "arc3", "arc3", null, ci1.entityReference().id(), EntityKind.CHANGE_INITIATIVE);

        // arc4 with linked entity as org unit
        createArc(4L, "arc4", "arc4", "ci1", ouIds.a, EntityKind.ORG_UNIT);

        // arc5 with no linked entity
        createArc(5L, "arc5", "arc5", "ci1", null, null);

        // important step to rebuild hierarchy
        rebuildHierarchy(EntityKind.CHANGE_INITIATIVE);
    }

    @Test
    public void getArcByIdTest() {
        ArchitectureRequiredChange arc = arcService.getById(1L);
        assertNotNull(arc);
    }

    @Test
    public void getArcByExternalIdTest() {
        ArchitectureRequiredChange arc = arcService.getByExternalId("arc1");
        assertNotNull(arc);
    }

    @Test
    public void getArcForLinkedEntityTest() {
        ChangeInitiative ci1 = ciService.findByExternalId("ci1").stream().findFirst().get();
        List<ArchitectureRequiredChange> arcs = arcService.findForLinkedEntityHierarchy(ci1.entityReference());
        assertTrue(arcs.size() == 3); //arc1, arc2, and arc3 must be linked via hierarchy

        // for arc linked to ou
        List<ArchitectureRequiredChange> arcsForOu = arcService.findForLinkedEntity(EntityReference.mkRef(EntityKind.ORG_UNIT, ouIds.a));
        assertTrue(arcsForOu.size() == 1);
    }

    private ChangeInitiative mkCi(Long id, String name, String extId, ChangeInitiativeKind kind, Long parentId, Long ouId) {
        ciHelper.createChangeInitiative(ImmutableChangeInitiative.builder()
                .id(id)
                .name(name)
                .description(name + " desc")
                .externalId(Optional.of(extId))
                .changeInitiativeKind(kind)
                .lifecyclePhase(LifecyclePhase.PRODUCTION)
                .organisationalUnitId(ouId)
                .parentId(Optional.ofNullable(parentId))
                .startDate(Date.valueOf(DateTimeUtilities.today().minusMonths(1)))
                .endDate(Date.valueOf(DateTimeUtilities.today().plusMonths(48)))
                .build());
        return changeInitiativeService.findByExternalId(extId).stream().findFirst().get();
    }


    private ArchitectureRequiredChange createArc(Long id, String title, String extId, String parentExtId, Long linkedEntityId, EntityKind linkedEntityKind) {
        arcHelper.createArchitectureRequiredChange(ImmutableArchitectureRequiredChange.builder()
                .id(id)
                .title(title)
                .description(title + " desc")
                .status("ACTIVE")
                .externalId(extId)
                .externalParentId(Optional.ofNullable(parentExtId))
                .linkedEntityKind(Optional.ofNullable(linkedEntityKind))
                .linkedEntityId(Optional.ofNullable(linkedEntityId))
                .createdBy("test-user")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .updatedBy("test-user")
                .provenance("test")
                .entityLifecycleStatus(EntityLifecycleStatus.ACTIVE)
                .build());

        return arcService.getByExternalId(extId);
    }

}
