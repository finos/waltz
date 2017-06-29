package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class OrgUnitExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(OrgUnitExtractor.class);

    private OrganisationalUnitService orgUnitService;


    @Autowired
    public OrgUnitExtractor(DSLContext dsl,
                            OrganisationalUnitService orgUnitService) {
        super(dsl);
        checkNotNull(orgUnitService, "orgUnitService cannot be null");

        this.orgUnitService = orgUnitService;
    }


    @Override
    public void register(String baseUrl) {
        get(mkPath(baseUrl, "org-units"), (request, response) ->
                writeFile("organisational-units.csv", extract(), response));
    }


    private CSVSerializer extract() {
        return csvWriter -> {
            csvWriter.writeHeader(
                    "id",
                    "parentId",
                    "name",
                    "description");

            orgUnitService.findAll()
                    .forEach(ou -> {
                        try {
                            csvWriter.write(
                                    ou.id().orElse(null),
                                    ou.parentId().orElse(null),
                                    ou.name(),
                                    ou.description());
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write ou: " + ou, ioe);
                        }
                    });
        };
    }

}
