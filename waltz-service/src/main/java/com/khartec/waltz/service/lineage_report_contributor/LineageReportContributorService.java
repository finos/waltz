package com.khartec.waltz.service.lineage_report_contributor;

import com.khartec.waltz.data.lineage_report_contribution.LineageReportContributorDao;
import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.model.lineage_report.LineageReportContributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
public class LineageReportContributorService {

    private final LineageReportContributorDao lineageReportContributorDao;


    @Autowired
    public LineageReportContributorService(LineageReportContributorDao lineageReportContributionDao) {
        this.lineageReportContributorDao = lineageReportContributionDao;
    }


    public List<LineageReportContributor> findContributors(long reportId) {
        return lineageReportContributorDao.findContributors(reportId);
    }

}
