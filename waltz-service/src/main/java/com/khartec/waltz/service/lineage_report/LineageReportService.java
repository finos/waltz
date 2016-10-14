package com.khartec.waltz.service.lineage_report;

import com.khartec.waltz.data.lineage_report.LineageReportDao;
import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.model.lineage_report.LineageReportContributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dwatkins on 12/10/2016.
 */
@Service
public class LineageReportService {
    private final LineageReportDao lineageReportDao;


    @Autowired
    public LineageReportService(LineageReportDao lineageReportDao) {
        this.lineageReportDao = lineageReportDao;
    }


    public LineageReport getById(long id) {
        return lineageReportDao.getById(id);
    }


    public List<LineageReport> findByPhysicalArticleId(long physicalArticleId) {
        return lineageReportDao.findByPhysicalArticleId(physicalArticleId);
    }


    public List<LineageReportContributor> findContributors(long reportId) {
        return lineageReportDao.findContributors(reportId);
    }
}
