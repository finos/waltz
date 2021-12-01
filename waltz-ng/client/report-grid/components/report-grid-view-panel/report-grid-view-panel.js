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

import template from "./report-grid-view-panel.html";
import {initialiseData} from "../../../common";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {mkChunks} from "../../../common/list-utils";
import {determineForegroundColor} from "../../../common/colors";
import {rgb} from "d3-color";
import {scaleLinear} from "d3-scale";
import extent from "d3-array/src/extent";
import ReportGridControlPanel from "../svelte/ReportGridControlPanel.svelte";
import {selectedGrid} from "../svelte/report-grid-store";

const bindings = {
    parentEntityRef: "<",
    // gridId: "<"
};

const initData = {
    categoryExtId: "CLOUD_READINESS",
    selectedCounterId: null,
    activeSummaryColRefs: [],
    filters: [],
    ReportGridControlPanel
};
const localStorageKey = "waltz-report-grid-view-section-last-id";

const nameCol = mkEntityLinkGridCell(
    "Name",
    "application",
    "none",
    "right",
    { pinnedLeft:true, width: 200});

const extIdCol = { field: "application.externalId", displayName: "Ext. Id", width: 100, pinnedLeft:true};

const lifecyclePhaseCol = {
    field: "application.lifecyclePhase",
    displayName: "Lifecycle Phase",
    width: 100,
    pinnedLeft: true,
    cellTemplate:`
        <div class="waltz-grid-report-cell"
            <span ng-bind="COL_FIELD | toDisplayName:'lifecyclePhase'"></span>
        </div>`
};

const unknownRating = {
    id: -1,
    color: "#f7f9f9",
    description: "This rating has not been provided",
    name: "Unknown",
    rating: "Z",
    position: 0
};


function mkPropNameForRef(ref) {
    return `${ref.kind}_${ref.id}`;
}


function mkPropNameForCellRef(x) {
    return `${x.columnEntityKind}_${x.columnEntityId}`;
}


function initialiseDataForRow(application, columnRefs) {
    return _.reduce(
        columnRefs,
        (acc, c) => {
            acc[c] = unknownRating;
            return acc;
        },
        {application});
}


function prepareColumnDefs(gridData) {
    const colDefs = _.get(gridData, ["definition", "columnDefinitions"], []);

    const mkColumnCustomProps = (c) =>  {
        switch (c.columnEntityReference.kind) {
            case "COST_KIND":
                return {
                    allowSummary: false,
                    cellTemplate:`
                        <div class="waltz-grid-report-cell"
                             style="text-align: right"
                             ng-style="{
                                'background-color': COL_FIELD.color,
                                'color': COL_FIELD.fontColor}">
                                <waltz-currency-amount amount="COL_FIELD.value"></waltz-currency-amount>
                        </div>`
                };
            case "INVOLVEMENT_KIND":
            case "SURVEY_QUESTION":
                return {
                    allowSummary: false,
                    width: 150,
                    toSearchTerm: d => _.get(d, [mkPropNameForRef(c.columnEntityReference), "text"], ""),
                    cellTemplate:`
                        <div class="waltz-grid-report-cell"
                             ng-class="{'wgrc-involvement-cell': COL_FIELD.text && ${c.columnEntityReference.kind === 'INVOLVEMENT_KIND'},
                                        'wgrc-survey-question-cell': COL_FIELD.text && ${c.columnEntityReference.kind === 'SURVEY_QUESTION'},
                                        'wgrc-no-data-cell': !COL_FIELD.text}"
                            <span ng-bind="COL_FIELD.text || '-'"
                                  ng-attr-title="{{COL_FIELD.text}}">
                            </span>
                        </div>`
                };
            default:
                return {
                    allowSummary: true,
                    toSearchTerm: d => _.get(d, [mkPropNameForRef(c.columnEntityReference), "name"], ""),
                    cellTemplate:
                        `<div class="waltz-grid-report-cell"
                              ng-bind="COL_FIELD.name"
                              uib-popover-html="COL_FIELD.comment"
                              popover-trigger="mouseenter"
                              popover-enable="COL_FIELD.comment != null"
                              popover-popup-delay="500"
                              popover-append-to-body="true"
                              popover-placement="left"
                              ng-style="{
                                'border-bottom-right-radius': COL_FIELD.comment ? '15% 50%' : 0,
                                'background-color': COL_FIELD.color,
                                'color': COL_FIELD.fontColor}">
                        </div>`
                };
        }
    };

    const additionalColumns = _
        .chain(colDefs)
        .map(c => {
            return Object.assign(
                {
                    field: mkPropNameForRef(c.columnEntityReference),
                    displayName: c.columnEntityReference.name,
                    columnDef: c,
                    width: 100,
                    headerTooltip: c.columnEntityReference.description,
                    enableSorting: false
                },
                mkColumnCustomProps(c));
        })
        .value();

    return _.concat([nameCol, extIdCol, lifecyclePhaseCol], additionalColumns);
}


function mkPopoverHtml(cellData, ratingSchemeItem) {
    const comment = cellData.comment;
    if (_.isEmpty(comment)) {
        return "";
    } else {
        const ratingDesc = ratingSchemeItem.description === ratingSchemeItem.name
            ? ""
            : `<div class='help-block'>${ratingSchemeItem.description}</div>`;

        return `
            <div class='small'>
                <label>Comment:</label> ${cellData.comment}
                <hr>
                <label>Rating:</label> ${ratingSchemeItem.name}
                ${ratingDesc}
            </div>`;
    }
}


function prepareTableData(gridData) {
    const appsById = _.keyBy(gridData.instance.applications, d => d.id);
    const ratingSchemeItemsById = _
        .chain(gridData.instance.ratingSchemeItems)
        .map(d => {
            const c = rgb(d.color);
            return Object.assign({}, d, { fontColor: determineForegroundColor(c.r, c.g, c.b)})
        })
        .keyBy(d => d.id)
        .value();

    const colDefs = _.get(gridData, ["definition", "columnDefinitions"], []);
    const columnRefs = _.map(colDefs, c => mkPropNameForRef(c.columnEntityReference));

    const costColorScalesByColumnEntityId = _
        .chain(gridData.instance.cellData)
        .filter(d => d.columnEntityKind === "COST_KIND")
        .groupBy(d => d.columnEntityId)
        .mapValues(v => scaleLinear()
            .domain(extent(v, d => d.value))
            .range(["#e2f5ff", "#86e4ff"]))
        .value();

    function mkTableCell(x) {
        switch(x.columnEntityKind) {
            case "COST_KIND":
                const color = costColorScalesByColumnEntityId[x.columnEntityId](x.value);
                return {
                    color: color,
                    value: x.value };
            case "INVOLVEMENT_KIND":
            case "SURVEY_QUESTION":
                return {
                    text: x.text };
            default:
                const ratingSchemeItem = ratingSchemeItemsById[x.ratingId];
                const popoverHtml = mkPopoverHtml(x, ratingSchemeItem);

                return Object.assign({}, ratingSchemeItem, { comment: popoverHtml });
        }}

    return _
        .chain(gridData.instance.cellData)
        .groupBy(d => d.applicationId)
        .map((xs, k) => _.reduce(
            xs,
            (acc, x) => {
                acc[mkPropNameForCellRef(x)] = mkTableCell(x);
                return acc;
            },
            initialiseDataForRow(appsById[k], columnRefs)))
        .orderBy(d => d.application.name)
        .value();
}


/**
 * We are not interested in some properties in the table data.
 * @param k
 * @returns {boolean}
 */
function isSummarisableProperty(k) {
    return ! (k === "application"
        || k === "$$hashKey"
        || k === "visible"
        || k === _.startsWith("COST_KIND"));
}


function refreshSummaries(tableData, columnDefinitions, ratingSchemeItems) {

    // increments a pair of counters referenced by `prop` in the object `acc`
    const accInc = (acc, prop, visible) => {
        const counts = _.get(acc, prop, {visible: 0, total:  0});
        counts.total++;
        if (visible) {
            counts.visible++;
        }
        acc[prop] = counts;
    };

    // reduce each value in an object representing a row by incrementing counters based on the property / value
    const reducer = (acc, row) => {
        _.forEach(
            row,
            (v, k) => isSummarisableProperty(k)
                ? accInc(
                    acc,
                    k + "#" + v.id,
                    _.get(row, ["visible"], true))
                : acc);
        return acc;
    };

    const ratingSchemeItemsById = _.keyBy(ratingSchemeItems, d => d.id);
    const columnsByRef = _.keyBy(columnDefinitions, d => mkPropNameForRef(d.columnEntityReference));

    return _
        .chain(tableData)
        .reduce(reducer, {})  // transform into a raw summary object for all rows
        .map((counts, k) => { // convert basic prop-val/count pairs in the summary object into a list of enriched objects
            const [colRef, ratingId] = _.split(k, "#");
            return {counterId: k, counts, colRef, rating: _.get(ratingSchemeItemsById, ratingId, unknownRating)};
        })
        .groupBy(d => d.colRef)  // group by the prop (colRef)
        .map((counters, colRef) => ({ // convert each prop group into a summary object with the actual column and a sorted set of counters
            column: columnsByRef[colRef],
            counters: _.orderBy(  // sort counters according to the rating ordering
                counters,
                [
                    c => c.rating.position,
                    c => c.rating.name
                ]),
            total: _.sumBy(counters, c => c.counts.total),
            totalVisible: _.sumBy(counters, c => c.counts.visible)
        }))
        .orderBy([  // order the summaries so they reflect the column order
            d => d.column.position,
            d => d.column.columnEntityReference.name
        ])
        .value();
}


/**
 * Returns a function which acts as a predicate to test rows against.
 *
 * The set of filters is first grouped by the row property they test.
 * For a row to pass, _at least_ one filter for _each_ group (prop)
 * needs to pass.
 *
 * @param filters
 * @returns {function(*=): boolean}
 */
function mkRowFilter(filters = []) {
    const filtersByPropName = _.groupBy(filters, f => f.propName);
    return td => _.every(
        filtersByPropName,
        (filtersForProp, prop) => {
            const propRating = _.get(td, [prop, "id"], null);
            return _.some(filtersForProp, f => propRating === f.ratingId);
        });
}


function controller($scope, serviceBroker, localStorageService) {

    const vm = initialiseData(this, initData);

    function refresh(filters = []) {
        vm.columnDefs = _.map(vm.allColumnDefs, cd => {
            if (cd.allowSummary){
                return Object.assign(cd, { menuItems: [
                    {
                        title: "Add to summary",
                        icon: "ui-grid-icon-info-circled",
                        action: function() {
                            vm.onAddSummary(cd);
                        },
                        context: vm
                    }
                ]})
            } else {
                return cd;
            }
        });

        const rowFilter = mkRowFilter(filters);

        const workingTableData =  _.map(
            vm.allTableData,
            d => Object.assign({}, d, { visible: rowFilter(d) }));

        vm.tableData = _.filter(workingTableData, d => d.visible);

        const summaries = refreshSummaries(
            workingTableData,
            vm.rawGridData.definition.columnDefinitions,
            vm.rawGridData.instance.ratingSchemeItems);

        vm.chunkedSummaryData = mkChunks(
            _.filter(
                summaries,
                d => _.includes(vm.activeSummaryColRefs, mkPropNameForRef(d.column.columnEntityReference))),
            4);

    }

    function loadGridData() {
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [vm.gridId, mkSelectionOptions(vm.parentEntityRef)], {force: true})
            .then(r => {
                console.log(r.data);
                vm.filters = [];
                vm.loading = false;
                vm.rawGridData = r.data;
                selectedGrid.set(r.data.definition);
                vm.allTableData = prepareTableData(vm.rawGridData);
                vm.allColumnDefs = prepareColumnDefs(vm.rawGridData);
                vm.activeSummaryColRefs = _
                    .chain(vm.rawGridData.definition.columnDefinitions)
                    .filter(d => d.usageKind === "SUMMARY")
                    .map(d => mkPropNameForRef(d.columnEntityReference))
                    .value();
                refresh();
            });
    }

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;

        if (vm.gridId) {

            vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);

            vm.loading = true;
            loadGridData();
        }
    };

    vm.onGridSelect = (grid) => {
        $scope.$applyAsync(() => {
            localStorageService.set(localStorageKey, grid.id);
            vm.gridId = grid.id;
            loadGridData();
        });
    };

    vm.onToggleFilter = (counter) => {
        if (_.some(vm.filters, f =>f.counterId === counter.counterId)) {
            vm.filters = _.reject(vm.filters, f =>f.counterId === counter.counterId);
            refresh(vm.filters);
        } else {
            const newFilter = {
                counterId: counter.counterId,
                propName: counter.colRef,
                ratingId: counter.rating.id
            };
            vm.filters = _.concat(vm.filters, [newFilter]);
            refresh(vm.filters);
        }
    };

    vm.onRemoveSummary = (summary) => {
        const refToRemove = mkPropNameForRef(summary.column.columnEntityReference);
        vm.activeSummaryColRefs = _.reject(vm.activeSummaryColRefs, ref => ref === refToRemove);
        // remove any filters which refer to the property used by this summary
        vm.filters = _.reject(vm.filters, f => f.propName === refToRemove);
        refresh(vm.filters);
    };

    vm.onAddSummary = (c) => {
        const colRef = mkPropNameForRef(c.columnDef.columnEntityReference);
        vm.activeSummaryColRefs = _.concat(vm.activeSummaryColRefs, [colRef]);
        refresh(vm.filters);
    };

    vm.isSelectedCounter = (cId) => {
        return _.some(vm.filters, f =>f.counterId === cId);
    };
}

controller.$inject = ["$scope", "ServiceBroker", "localStorageService"];

const component = {
    controller,
    bindings,
    template
};

export default {
    id: "waltzReportGridViewPanel",
    component,
}