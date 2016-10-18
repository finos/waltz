package com.khartec.waltz.data.lineage_report;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.lineage_report.*;
import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.schema.tables.*;
import com.khartec.waltz.schema.tables.LineageReportContributor;
import com.khartec.waltz.schema.tables.records.LineageReportRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.LineageReport.LINEAGE_REPORT;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


@Repository
public class LineageReportDao {

    private static final com.khartec.waltz.schema.tables.LineageReport report = LINEAGE_REPORT.as("report");


    public static final RecordMapper<Record, LineageReport> TO_DOMAIN_MAPPER = r -> {
        LineageReportRecord record = r.into(LINEAGE_REPORT);
        return ImmutableLineageReport.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .specificationId(record.getSpecificationId())
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public LineageReportDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public LineageReport getById(long id) {
        return dsl.select(report.fields())
                .from(report)
                .where(report.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<LineageReport> findByPhysicalSpecificationId(long specificationId) {
        return dsl.select(report.fields())
                .from(report)
                .where(report.SPECIFICATION_ID.eq(specificationId))
                .orderBy(report.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<LineageReportDescriptor> findReportsContributedToBySpecification(long specificationId) {
        Application app = Application.APPLICATION.as("app");
        PhysicalSpecification targetSpecification = PHYSICAL_SPECIFICATION.as("targetSpecification");
        com.khartec.waltz.schema.tables.LineageReport report = com.khartec.waltz.schema.tables.LineageReport.LINEAGE_REPORT.as("report");

        LineageReportContributor contributor = LineageReportContributor.LINEAGE_REPORT_CONTRIBUTOR.as("contributor");
        PhysicalFlow physFlow = PhysicalFlow.PHYSICAL_FLOW.as("physFlow");

        SelectConditionStep<Record1<Long>> mentionedReportIds = DSL
                .selectDistinct(contributor.LINEAGE_REPORT_ID)
                .from(contributor)
                .innerJoin(physFlow)
                .on(physFlow.ID.eq(contributor.PHYSICAL_FLOW_ID))
                .where(physFlow.SPECIFICATION_ID.eq(specificationId));

        return dsl
                .select(report.ID,
                        report.NAME,
                        report.DESCRIPTION,
                        targetSpecification.ID,
                        targetSpecification.NAME,
                        targetSpecification.DESCRIPTION,
                        app.ID,
                        app.NAME,
                        app.DESCRIPTION)
                .from(report)
                .innerJoin(targetSpecification)
                .on(targetSpecification.ID.eq(report.SPECIFICATION_ID))
                .innerJoin(app)
                .on(app.ID.eq(targetSpecification.OWNING_APPLICATION_ID))
                .where(report.ID.in(mentionedReportIds))
                .fetch(r -> ImmutableLineageReportDescriptor.builder()
                        .reportReference(mkRef(
                                EntityKind.LINEAGE_REPORT,
                                r.getValue(report.ID),
                                r.getValue(report.NAME),
                                r.getValue(report.DESCRIPTION)))
                        .specificationReference(mkRef(
                                EntityKind.PHYSICAL_SPECIFICATION,
                                r.getValue(targetSpecification.ID),
                                r.getValue(targetSpecification.NAME),
                                r.getValue(targetSpecification.DESCRIPTION)))
                        .applicationReference(mkRef(
                                EntityKind.APPLICATION,
                                r.getValue(app.ID),
                                r.getValue(app.NAME),
                                r.getValue(app.DESCRIPTION)))
                        .build());

    }

    public long create(LineageReportCreateCommand command, String username) {
        LineageReportRecord record = dsl.newRecord(LINEAGE_REPORT);
        record.setProvenance("waltz");
        record.setDescription(mkSafe(command.description()));
        record.setName(command.name());
        record.setSpecificationId(command.specificationId());

        record.store();

        return record.getId();
    }

    public boolean update(LineageReportChangeCommand cmd) {

        LineageReportRecord record = dsl.newRecord(LINEAGE_REPORT);

        record.setId(cmd.id());
        record.changed(LINEAGE_REPORT.ID, false);

        cmd.name().ifPresent(change -> record.setName(change.newVal()));
        cmd.description().ifPresent(change -> record.setDescription(change.newVal()));

        int count = record.update();

        return count == 1;

    }
}

