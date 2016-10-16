package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.lineage_report.LineageReport;
import com.khartec.waltz.model.lineage_report.LineageReportDescriptor;
import com.khartec.waltz.service.lineage_report.LineageReportService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

/**
 * Created by dwatkins on 12/10/2016.
 */
@Service
public class LineageReportEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "lineage-report");


    private final LineageReportService lineageReportService;


    @Autowired
    public LineageReportEndpoint(LineageReportService lineageReportService) {
        checkNotNull(lineageReportService, "lineageReportService cannot be null");
        this.lineageReportService = lineageReportService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        String findByPhysicalArticleIdPath = mkPath(
                BASE_URL,
                "physical-article",
                ":id");

        String findReportsContributedToByArticlePath = mkPath(
                BASE_URL,
                "physical-article",
                ":id",
                "contributions");

        DatumRoute<LineageReport> getByIdRoute =
                (request, response) -> lineageReportService.getById(getId(request));


        ListRoute<LineageReport> findByPhysicalArticleIdRoute =
                (request, response) -> lineageReportService.findByPhysicalArticleId(getId(request));

        ListRoute<LineageReportDescriptor> findReportsContributedToByArticleRoute =
                (request, response) -> lineageReportService.findReportsContributedToByArticle(getId(request));


        // .. -> Reports
        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByPhysicalArticleIdPath, findByPhysicalArticleIdRoute);
        getForList(findReportsContributedToByArticlePath, findReportsContributedToByArticleRoute);
    }

}
