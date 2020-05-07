/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {isEmpty} from "../../../common/index";
import {toOffsetMap} from "../../../common/list-utils";


export function filterData(data, qry, excludedRatings = []) {
    const origData = _.cloneDeep(data);

    if (isEmpty(qry) && isEmpty(excludedRatings)) {
        return origData;
    }

    qry = _.toLower(qry);

    const qryMatcher = _.isEmpty(qry)
        ? () => true // no search string, therefore matches everything
        : n => {
            const searchStr = _.get(n, ["searchTargetStr"], "");
            return searchStr.indexOf(qry) > -1;
        };

    const exclusionMatcher = _.isEmpty(excludedRatings)
        ? () => false // no exclusions, therefore nothing excluded
        : n => {
            const rating = _.get(n, ["state", "rating"], null);
            return _.includes(excludedRatings, rating);
        };

    const nodeMatchFn = n => {
        const qryMatches = qryMatcher(n);
        const isExcluded = exclusionMatcher(n);
        return qryMatches && !isExcluded;
    };

    const filterNodeGridFn = nodeGrid => _.filter(nodeGrid, nodeMatchFn);
    const filterRowFn = nodeGrids =>  _.map(nodeGrids, filterNodeGridFn);
    return _.map(data, filterRowFn);
}


function prepareAxisHeadings(scenarioDefinition, measurablesById, hiddenAxes = []) {
    const hiddenAxisIds = _.map(hiddenAxes, "id");
    return _.chain(scenarioDefinition.axisDefinitions)
        .filter(d => !_.includes(hiddenAxisIds, d.domainItem.id))
        .map(d => {
            const measurable = measurablesById[d.domainItem.id];
            if (! measurable) {
                console.log("Cannot draw column/row as it references an unknown measurable: ", { measurableId: d.domainItem.id, axisDefinition: d });
                return null;
            }
            return {
                id: measurable.id,
                name: measurable.name,
                description: measurable.description,
                axisOrientation: d.axisOrientation,
                position: d.position,
                data: measurable
            };
        })
        .compact()
        .orderBy(d => d.position)
        .groupBy(d => d.axisOrientation)
        .value();
}

export function getChangeScenarioCommand(scenarioDefn, appId, columnId, rowId, rating=null, comment=null, currentRating=null) {
    return {
        scenarioId: scenarioDefn.scenario.id,
        appId: appId,
        columnId: columnId,
        rowId: rowId,
        ratingSchemeId: scenarioDefn.roadmap.ratingSchemeId,
        rating: rating,
        comment: comment,
        previousRating: currentRating
    };
}


export function prepareData(scenarioDefinition, applications = [], measurables = [], hiddenAxes = []) {
    const applicationsById = _.keyBy(applications, "id");
    const measurablesById = _.keyBy(measurables, "id");
    const axisHeadings = prepareAxisHeadings(scenarioDefinition, measurablesById, hiddenAxes);

    const columnHeadings = axisHeadings["COLUMN"] || [];
    const rowHeadings = axisHeadings["ROW"] || [];

    const colOffsets = toOffsetMap(columnHeadings);
    const rowOffsets = toOffsetMap(rowHeadings);

    const baseRowData = _.times(rowHeadings.length, () => _.times(columnHeadings.length, () => []));

    const rowData = _.reduce(scenarioDefinition.ratings, (acc, d) => {
        const rowId = d.row.id;
        const columnId = d.column.id;
        const appId = d.item.id;
        const id = `${appId}_${rowId}_${columnId}`;

        const app = applicationsById[appId];

        const rowOffset = rowOffsets[rowId];
        const colOffset = colOffsets[columnId];

        const row = acc[rowOffset] || [];
        const col = row[colOffset] || [];

        const domainCoordinates = {
            row: measurablesById[d.row.id],
            column: measurablesById[d.column.id]
        };

        const nodeData = {
            id ,
            node: Object.assign({}, app, { externalId: app.assetCode }),
            domainCoordinates,
            state: {
                rating: d.rating,
                comment: d.description
            },
            searchTargetStr: `${app.name} ${app.assetCode}`.toLowerCase()
        };

        row[colOffset] = _.concat(col, [nodeData]);
        acc[rowOffset] = row;

        return acc;
    }, baseRowData);

    return {
        columnHeadings,
        rowHeadings,
        rowData
    };
}
