/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.Measurable;
import com.khartec.waltz.schema.tables.ScenarioAxisItem;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class ScenarioExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioExtractor.class);

    @Autowired
    public ScenarioExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        get(mkPath("data-extract", "scenario", ":id"), (request, response) -> extract(getId(request), response));
    }


    private Object extract(long scenarioId, Response response) throws Exception {

        Measurable rowMeasurable = MEASURABLE.as("rowMeasurable");
        Measurable colMeasurable = MEASURABLE.as("colMeasurable");
        ScenarioAxisItem rowAxisItem = SCENARIO_AXIS_ITEM.as("rowAxisItem");
        ScenarioAxisItem colAxisItem = SCENARIO_AXIS_ITEM.as("colAxisItem");

        Condition rowMeasurableJoin = rowMeasurable.ID.eq(SCENARIO_RATING_ITEM.ROW_ID)
                .and(SCENARIO_RATING_ITEM.ROW_KIND.eq(EntityKind.MEASURABLE.name()));

        Condition colMeasurableJoin = colMeasurable.ID.eq(SCENARIO_RATING_ITEM.COLUMN_ID)
                .and(SCENARIO_RATING_ITEM.COLUMN_KIND.eq(EntityKind.MEASURABLE.name()));

        Condition appJoin = APPLICATION.ID.eq(SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID)
                .and(SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND.eq(EntityKind.APPLICATION.name()));

        Condition rowAxisItemJoin = rowAxisItem.DOMAIN_ITEM_ID.eq(SCENARIO_RATING_ITEM.ROW_ID)
                .and(rowAxisItem.ORIENTATION.eq(AxisOrientation.ROW.name()))
                .and(rowAxisItem.SCENARIO_ID.eq(SCENARIO_RATING_ITEM.SCENARIO_ID));

        Condition colAxisItemJoin = colAxisItem.DOMAIN_ITEM_ID.eq(SCENARIO_RATING_ITEM.COLUMN_ID)
                .and(colAxisItem.ORIENTATION.eq(AxisOrientation.COLUMN.name()))
                .and(colAxisItem.SCENARIO_ID.eq(SCENARIO_RATING_ITEM.SCENARIO_ID));

        SelectConditionStep<Record6<String, String, String, String, String, String>> qry = dsl
                .select(
                    APPLICATION.NAME,
                    APPLICATION.ASSET_CODE,
                    colMeasurable.NAME,
                    rowMeasurable.NAME,
                    RATING_SCHEME_ITEM.NAME,
                    SCENARIO_RATING_ITEM.DESCRIPTION)
                .from(SCENARIO_RATING_ITEM)
                .innerJoin(APPLICATION)
                .on(appJoin)
                .innerJoin(rowMeasurable)
                .on(rowMeasurableJoin)
                .innerJoin(colMeasurable)
                .on(colMeasurableJoin)
                .innerJoin(rowAxisItem)
                .on(rowAxisItemJoin)
                .innerJoin(colAxisItem)
                .on(colAxisItemJoin)
                .innerJoin(SCENARIO)
                .on(SCENARIO.ID.eq(scenarioId))
                .innerJoin(ROADMAP)
                .on(ROADMAP.ID.eq(SCENARIO.ROADMAP_ID))
                .innerJoin(RATING_SCHEME_ITEM)
                .on(RATING_SCHEME_ITEM.SCHEME_ID.eq(ROADMAP.RATING_SCHEME_ID).and(RATING_SCHEME_ITEM.CODE.eq(SCENARIO_RATING_ITEM.RATING)))
                .where(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(scenarioId));

        System.out.println(qry);

        CSVSerializer serializer = csvWriter -> {
            csvWriter.writeHeader(
                    "Application",
                    "App Asset Code",
                    "Column",
                    "Row",
                    "Rating",
                    "Description");

            qry.fetch().forEach(r -> {
                try {
                    csvWriter.write(
                            r.get(APPLICATION.NAME),
                            r.get(APPLICATION.ASSET_CODE),
                            r.get(colMeasurable.NAME),
                            r.get(rowMeasurable.NAME),
                            r.get(RATING_SCHEME_ITEM.NAME),
                            r.get(SCENARIO_RATING_ITEM.DESCRIPTION));
                } catch (IOException ioe) {
                    LOG.warn("Failed to write scenario data: " + r, ioe);
                }
            });
        };

        String scenarioName = dsl
                .select(SCENARIO.NAME)
                .from(SCENARIO)
                .where(SCENARIO.ID.eq(scenarioId))
                .fetchOne(SCENARIO.NAME);

        String suggestedFilename = "scenario-" + scenarioName + ".csv";

        return writeFile(suggestedFilename, serializer, response);
    }

}
