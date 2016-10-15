package com.khartec.waltz.data.lineage_report;

import com.khartec.waltz.model.lineage_report.ImmutableLineageReport;
import com.khartec.waltz.model.lineage_report.ImmutableLineageReportContributor;
import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.model.lineage_report.LineageReportContributor;
import com.khartec.waltz.schema.tables.records.LineageReportContributorRecord;
import com.khartec.waltz.schema.tables.records.LineageReportRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.LineageReport.LINEAGE_REPORT;
import static com.khartec.waltz.schema.tables.LineageReportContributor.LINEAGE_REPORT_CONTRIBUTOR;
import static com.khartec.waltz.schema.tables.PhysicalDataFlow.PHYSICAL_DATA_FLOW;


@Repository
public class LineageReportDao {

    private static final RecordMapper<Record, LineageReportContributor> TO_CONTRIB_DOMAIN_MAPPER = r -> {
        LineageReportContributorRecord record = r.into(LINEAGE_REPORT_CONTRIBUTOR);
        return ImmutableLineageReportContributor.builder()
                .description(record.getDescription())
                .physicalDataFlowId(record.getPhysicalFlowId())
                .reportId(record.getLineageReportId())
                .build();
    };


    private static final RecordMapper<Record, LineageReport> TO_REPORT_DOMAIN_MAPPER = r -> {
        LineageReportRecord record = r.into(LINEAGE_REPORT);
        return ImmutableLineageReport.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .physicalArticleId(record.getPhysicalArticleId())
                .provenance(record.getProvenance())
                .build();
    };

    private final static com.khartec.waltz.schema.tables.LineageReport rep = LINEAGE_REPORT.as("rep");
    private final static com.khartec.waltz.schema.tables.LineageReportContributor contrib = LINEAGE_REPORT_CONTRIBUTOR.as("contrib");
    private final static com.khartec.waltz.schema.tables.PhysicalDataFlow physicalFlows = PHYSICAL_DATA_FLOW.as("physicalFlows");
    private final static com.khartec.waltz.schema.tables.DataFlow logicalFlows = DATA_FLOW.as("logicalFlows");


    private final DSLContext dsl;


    @Autowired
    public LineageReportDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public LineageReport getById(long id) {
        return dsl.select(rep.fields())
                .from(rep)
                .where(rep.ID.eq(id))
                .fetchOne(TO_REPORT_DOMAIN_MAPPER);
    }


    public List<LineageReport> findByPhysicalArticleId(long physicalArticleId) {
        return dsl.select(rep.fields())
                .from(rep)
                .where(rep.PHYSICAL_ARTICLE_ID.eq(physicalArticleId))
                .orderBy(rep.NAME)
                .fetch(TO_REPORT_DOMAIN_MAPPER);
    }


    public List<LineageReportContributor> findContributors(long reportId) {
        return dsl
                .select(contrib.fields())
                .where(rep.ID.eq(reportId))
                .fetch(TO_CONTRIB_DOMAIN_MAPPER);
    }
}
