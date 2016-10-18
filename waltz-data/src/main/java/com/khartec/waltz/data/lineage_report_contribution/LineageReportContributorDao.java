package com.khartec.waltz.data.lineage_report_contribution;

import com.khartec.waltz.model.lineage_report.ImmutableLineageReportContributor;
import com.khartec.waltz.model.lineage_report.LineageReportContributor;
import com.khartec.waltz.schema.tables.records.LineageReportContributorRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.LineageReport.LINEAGE_REPORT;
import static com.khartec.waltz.schema.tables.LineageReportContributor.LINEAGE_REPORT_CONTRIBUTOR;

@Repository
public class LineageReportContributorDao {

    private final static com.khartec.waltz.schema.tables.LineageReport report = LINEAGE_REPORT.as("report");
    private final static com.khartec.waltz.schema.tables.LineageReportContributor contrib = LINEAGE_REPORT_CONTRIBUTOR.as("contrib");

    private static final RecordMapper<Record, LineageReportContributor> TO_DOMAIN_MAPPER = r -> {
        LineageReportContributorRecord record = r.into(LINEAGE_REPORT_CONTRIBUTOR);
        return ImmutableLineageReportContributor.builder()
                .description(record.getDescription())
                .physicalDataFlowId(record.getPhysicalFlowId())
                .reportId(record.getLineageReportId())
                .build();
    };


    private final DSLContext dsl;



    @Autowired
    public LineageReportContributorDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<LineageReportContributor> findContributors(long reportId) {
        return dsl
                .select(contrib.fields())
                .where(report.ID.eq(reportId))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
