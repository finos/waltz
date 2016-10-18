package com.khartec.waltz.service.lineage_report;

import com.khartec.waltz.data.lineage_report.LineageReportDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.lineage_report.LineageReportChangeCommand;
import com.khartec.waltz.model.lineage_report.LineageReportCreateCommand;
import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.model.lineage_report.LineageReportDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LineageReportService {

    private static final Logger LOG = LoggerFactory.getLogger(LineageReportService.class);

    private final LineageReportDao lineageReportDao;


    @Autowired
    public LineageReportService(LineageReportDao lineageReportDao) {
        this.lineageReportDao = lineageReportDao;
    }


    public LineageReport getById(long id) {
        return lineageReportDao.getById(id);
    }


    public List<LineageReport> findByPhysicalSpecificationId(long physicalSpecificationId) {
        return lineageReportDao.findByPhysicalSpecificationId(physicalSpecificationId);
    }


    public List<LineageReportDescriptor> findReportsContributedToBySpecification(long physicalSpecificationId) {
        return lineageReportDao.findReportsContributedToBySpecification(physicalSpecificationId);
    }


    public long create(LineageReportCreateCommand command, String username) {
        LOG.info("Creating lineage report: {} for {}", command, username);
        return lineageReportDao.create(command, username);
    }


    public CommandResponse<LineageReportChangeCommand> update(LineageReportChangeCommand cmd) {
        LOG.info("Updating lineage report: {}", cmd);
        boolean success = lineageReportDao.update(cmd);
        return ImmutableCommandResponse.<LineageReportChangeCommand>builder()
                .originalCommand(cmd)
                .entityReference(EntityReference.mkRef(EntityKind.LINEAGE_REPORT, cmd.id()))
                .outcome(success ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .build();
    }

}
