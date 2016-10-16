package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.lineage_report_contributor.LineageReportContributorService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

/**
 * Created by dwatkins on 12/10/2016.
 */
@Service
public class LineageReportContributorEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "lineage-report-contributor");

    private final LineageReportContributorService lineageReportContributorService;


    @Autowired
    public LineageReportContributorEndpoint(LineageReportContributorService lineageReportContributorService) {
        checkNotNull(lineageReportContributorService, "lineageReportContributorService cannot be null");
        this.lineageReportContributorService = lineageReportContributorService;
    }


    @Override
    public void register() {

        String findContributorsByArticleIdPath = mkPath(
                BASE_URL,
                "physical-article",
                ":id");

        String findContributorsByReportIdPath = mkPath(
                BASE_URL,
                "report",
                ":id");

        getForList(
                findContributorsByArticleIdPath,
                (request, response) -> Collections.emptyList());

        getForList(
                findContributorsByReportIdPath,
                (request, response) -> Collections.emptyList());
    }

}
