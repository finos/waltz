/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringWriter;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static spark.Spark.get;

@Service
public class DataExtractEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("data-extract");
    private static final Logger LOG = LoggerFactory.getLogger(DataExtractEndpoint.class);


    private final OrganisationalUnitService orgUnitService;


    @Autowired
    public DataExtractEndpoint(OrganisationalUnitService orgUnitService) {
        checkNotNull(orgUnitService, "orgUnitService must not be null");

        this.orgUnitService = orgUnitService;
    }


    @Override
    public void register() {
        registerOrgUnitsExtract();
        registerCapabilitiesExtract();
    }


    private void registerCapabilitiesExtract() {
        registerExtractor("capabilities", "capabilities.csv", (csvWriter) -> {
            csvWriter.writeHeader(
                    "id",
                    "parentId",
                    "name",
                    "description");
        });
    }


    private void registerOrgUnitsExtract() {
        registerExtractor("org-units", "organisational-units.csv", csvWriter -> {
            csvWriter.writeHeader(
                    "id",
                    "parentId",
                    "name",
                    "kind",
                    "description");

            orgUnitService.findAll()
                    .forEach(ou -> {
                        try {
                            csvWriter.write(
                                    ou.id().orElse(null),
                                    ou.parentId().orElse(null),
                                    ou.name(),
                                    ou.kind().name(),
                                    ou.description()
                            );
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write ou: " + ou, ioe);
                        }
                    });
        });
    }


    public void registerExtractor(String endpoint, String suggestedFilename, Extractor extractor) {
        get(WebUtilities.mkPath(BASE_URL, endpoint), (request, response) -> {
            response.type(MimeTypes.Type.TEXT_PLAIN.name());
            response.header("Content-disposition", "attachment; filename="+suggestedFilename);

            StringWriter bodyWriter = new StringWriter();
            CsvListWriter csvWriter = new CsvListWriter(bodyWriter, CsvPreference.EXCEL_PREFERENCE);

            extractor.accept(csvWriter);

            csvWriter.flush();
            return bodyWriter.toString();
        });
    }

}
