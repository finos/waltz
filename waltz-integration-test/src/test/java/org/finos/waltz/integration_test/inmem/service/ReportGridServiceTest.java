package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.InvolvementHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.service.report_grid.ReportGridMemberService;
import org.finos.waltz.service.report_grid.ReportGridService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.schema.Tables.REPORT_GRID_COLUMN_DEFINITION;
import static org.junit.Assert.*;

@Service
public class ReportGridServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DSLContext dsl;


    @Autowired
    private ReportGridService reportGridService;

    @Autowired
    private ReportGridMemberService reportGridMemberService;

    @Autowired
    private InvolvementHelper involvementHelper;


    @Test
    public void canCreateAReportGrid() throws InsufficientPrivelegeException {
        ReportGridDefinition def = mkGrid();
        assertNotNull("expected a report grid definition is not null", def);
        assertTrue("id should be set (positive integer)", def.id().get() > 0);
    }


    @Test
    public void canGetColumnDefinitionsForGrid() throws InsufficientPrivelegeException {
        ReportGridDefinition def = mkGrid();
        assertEquals(1, def.columnDefinitions().size());
    }


    @Test
    public void cannotRemoveANonExistentReportGrid() throws InsufficientPrivelegeException {
        assertThrows(
                "Cannot remove a non existent report grid",
                NotFoundException.class,
                () -> reportGridService.remove(-1, mkName("admin")));
    }


    @Test
    public void cannotRemoveReportGridYouDoNotOwn() throws InsufficientPrivelegeException {
        ReportGridDefinition grid = mkGrid();
        assertThrows(
                "Cannot remove a report grid the user does not own",
                InsufficientPrivelegeException.class,
                () -> reportGridService.remove(grid.id().get(), mkName("someone_else")));
    }


    @Test
    public void canRemoveGridThatWeDoOwn() throws InsufficientPrivelegeException {
        ReportGridDefinition grid = mkGrid();
        Set<ReportGridMember> members = reportGridMemberService.findByGridId(grid.id().get());
        Optional<ReportGridMember> maybeOwner = maybeFirst(
                members,
                m -> m.role() == ReportGridMemberRole.OWNER);
        String ownerId = maybeOwner
                .map(ReportGridMember::userId)
                .orElseThrow(() -> new AssertionError("Should have an owner for a newly created grid"));

        assertTrue("grid should have been removed", reportGridService.remove(grid.id().get(), ownerId));
        assertTrue("members should have been removed", reportGridMemberService.findByGridId(grid.id().get()).isEmpty());
        assertFalse("cannot find grid after it's been removed", find(reportGridService.findAll(), g -> g.id().equals(grid.id())).isPresent());  //check it's really gone

        assertThrows(
                "Cannot remove a report grid we have already removed",
                NotFoundException.class,
                () -> reportGridService.remove(grid.id().get(), ownerId));

        Record1<Integer> count = dsl
                .selectCount()
                .from(REPORT_GRID_COLUMN_DEFINITION)
                .where(REPORT_GRID_COLUMN_DEFINITION.REPORT_GRID_ID.eq(grid.id().get()))
                .fetchOne();

        assertEquals(Integer.valueOf(0), count.value1());

    }


    // -- HELPERS --------------

    private ReportGridDefinition mkGrid() throws InsufficientPrivelegeException {
        ReportGridCreateCommand cmd = ImmutableReportGridCreateCommand.builder()
                .name(mkName("testReport"))
                .externalId(mkName("extId"))
                .build();

        String admin = mkName("admin");
        ReportGridDefinition def = reportGridService.create(cmd, admin);

        long invKind = involvementHelper.mkInvolvementKind(mkName("dummyInv"));

        ReportGridColumnDefinition colDef = ImmutableReportGridColumnDefinition
                .builder()
                .columnEntityReference(EntityReference.mkRef(EntityKind.INVOLVEMENT_KIND, invKind))
                .position(10)
                .build();

        ReportGridColumnDefinitionsUpdateCommand colCmd = ImmutableReportGridColumnDefinitionsUpdateCommand
                .builder()
                .columnDefinitions(asSet(colDef))
                .build();


        return reportGridService.updateColumnDefinitions(
                def.id().get(),
                colCmd,
                admin);
    }


}
