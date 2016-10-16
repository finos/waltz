package com.khartec.waltz.data.lineage_report;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.lineage_report.ImmutableLineageReport;
import com.khartec.waltz.model.lineage_report.ImmutableLineageReportDescriptor;
import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.model.lineage_report.LineageReportDescriptor;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.LineageReportContributor;
import com.khartec.waltz.schema.tables.PhysicalDataArticle;
import com.khartec.waltz.schema.tables.PhysicalDataFlow;
import com.khartec.waltz.schema.tables.records.LineageReportRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.LineageReport.LINEAGE_REPORT;
import static com.khartec.waltz.schema.tables.PhysicalDataArticle.PHYSICAL_DATA_ARTICLE;


@Repository
public class LineageReportDao {

    private static final com.khartec.waltz.schema.tables.LineageReport report = LINEAGE_REPORT.as("report");


    public static final RecordMapper<Record, LineageReport> TO_DOMAIN_MAPPER = r -> {
        LineageReportRecord record = r.into(LINEAGE_REPORT);
        return ImmutableLineageReport.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .physicalArticleId(record.getPhysicalArticleId())
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


    public List<LineageReport> findByPhysicalArticleId(long physicalArticleId) {
        return dsl.select(report.fields())
                .from(report)
                .where(report.PHYSICAL_ARTICLE_ID.eq(physicalArticleId))
                .orderBy(report.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<LineageReportDescriptor> findReportsContributedToByArticle(long physicalArticleId) {
        Application app = Application.APPLICATION.as("app");
        PhysicalDataArticle targetArticle = PHYSICAL_DATA_ARTICLE.as("targetArticle");
        com.khartec.waltz.schema.tables.LineageReport report = com.khartec.waltz.schema.tables.LineageReport.LINEAGE_REPORT.as("report");

        LineageReportContributor contributor = LineageReportContributor.LINEAGE_REPORT_CONTRIBUTOR.as("contributor");
        PhysicalDataFlow physFlow = PhysicalDataFlow.PHYSICAL_DATA_FLOW.as("physFlow");

        SelectConditionStep<Record1<Long>> mentionedReportIds = DSL
                .selectDistinct(contributor.LINEAGE_REPORT_ID)
                .from(contributor)
                .innerJoin(physFlow)
                .on(physFlow.ID.eq(contributor.PHYSICAL_FLOW_ID))
                .where(physFlow.ARTICLE_ID.eq(physicalArticleId));

        return dsl
                .select(report.ID,
                        report.NAME,
                        report.DESCRIPTION,
                        targetArticle.ID,
                        targetArticle.NAME,
                        targetArticle.DESCRIPTION,
                        app.ID,
                        app.NAME,
                        app.DESCRIPTION)
                .from(report)
                .innerJoin(targetArticle)
                .on(targetArticle.ID.eq(report.PHYSICAL_ARTICLE_ID))
                .innerJoin(app)
                .on(app.ID.eq(targetArticle.OWNING_APPLICATION_ID))
                .where(report.ID.in(mentionedReportIds))
                .fetch(r -> ImmutableLineageReportDescriptor.builder()
                        .reportReference(mkRef(
                                EntityKind.LINEAGE_REPORT,
                                r.getValue(report.ID),
                                r.getValue(report.NAME),
                                r.getValue(report.DESCRIPTION)))
                        .articleReference(mkRef(
                                EntityKind.PHYSICAL_DATA_ARTICLE,
                                r.getValue(targetArticle.ID),
                                r.getValue(targetArticle.NAME),
                                r.getValue(targetArticle.DESCRIPTION)))
                        .applicationReference(mkRef(
                                EntityKind.APPLICATION,
                                r.getValue(app.ID),
                                r.getValue(app.NAME),
                                r.getValue(app.DESCRIPTION)))
                        .build());

    }
}

