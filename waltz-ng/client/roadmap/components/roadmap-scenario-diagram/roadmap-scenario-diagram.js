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

import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {
    getChangeScenarioCommand,
    prepareData
} from "../../../scenario/components/scenario-diagram/scenario-diagram-data-utils";
import {initialiseData} from "../../../common";
import {event, select, selectAll} from "d3-selection";
import roles from "../../../user/system-roles";

import template from "./roadmap-scenario-diagram.html";
import {getDefaultRating} from "../../../ratings/rating-utils";
import {kindToViewState} from "../../../common/link-utils";


const bindings = {
    scenarioId: "<",
    onCancel: "<"
};


const modes = {
    EDIT: "EDIT",
    VIEW: "VIEW"
};


const initialState = {
    dialog: null,
    handlers: {},
    hiddenAxes: [],
    lastRatings: {},
    layoutOptions: {
        colWidths: {}, // { domainItemId: width }
    },
    mode: modes.VIEW,
    permissions: {
        admin: false,
        edit: false
    }
};


const dialogs = {
    ADD_APPLICATION: "ADD_APPLICATION",
    EDIT_CELL: "EDIT_CELL"
};


const styles = {
    INFOPOP: "wrsd-infopop",
    INFOPOP_CLOSE: "infopop-close",
};


const component = {
    bindings,
    template,
    controller
};


const divider = {
    divider: true
};


function determineApplicationPickList(applications = [], ratings = [], column, row) {

    const usedAppIds = _
        .chain(ratings)
        .filter(r => r.column.id ===  column.id && r.row.id === row.id)
        .map(r => r.item.id)
        .value();

    return _
        .chain(applications)
        .reject(a => _.includes(usedAppIds, a.id))
        .orderBy(a => a.name.toLowerCase())
        .value();
}


function hideInfoPopup() {
    select(`.${styles.INFOPOP}`)
        .style("display", "none");
}


function showInfoPopup(html) {
    return select(`.${styles.INFOPOP}`)
        .style("left", (event.pageX - 2) + "px")
        .style("top", (event.pageY - 2) + "px")
        .style("display", "block")
        .html(html)
        .select(`.${styles.INFOPOP_CLOSE}`)
        .on("click", () => hideInfoPopup());
}


function mkAction(title, action) {
    return {
        title,
        action
    };
}


function mkDialogStyle() {
    return {
        display: "block",
        position: "absolute",
        left: `${event.pageX - 2}px`,
        top: `${event.pageY - 100}px`
    };
}


/**
 * Calculates the key to use when saving col width information to local storage
 *
 * @param scenarioId
 * @returns {string}  local storage key
 */
function mkColWidthStorageKey(scenarioId) {
    return `waltz-scenario-col-widths-${scenarioId}`;
}


function mkLayoutOptions(domainCols = [], suppliedColWidths = {}) {
    const comfortableColCount = 8;

    const colsPerDomainCol = Math.max(Math.floor(comfortableColCount / Math.max(_.size(domainCols), 1)), 1);

    const computedColWidths = _.reduce(
        domainCols,
        (acc, domainCol) => { acc[domainCol.id] = colsPerDomainCol; return acc; }, {});

    const colWidths = Object.assign({}, computedColWidths, suppliedColWidths);

    return {
        colWidths
    };
}


function controller($q,
                    $timeout,
                    localStorageService,
                    notification,
                    serviceBroker,
                    userService,
                    $window,
                    $state) {

    const vm = initialiseData(this, initialState);

    function prepData() {
        return prepareData(vm.scenarioDefn, vm.applications, vm.measurables, vm.hiddenAxes);
    }

    function alterColWidth(domainId, delta) {
        const path = ["layoutOptions", "colWidths", domainId];
        const currentWidth = _.get(vm, path);
        const newWidth = Math.max(1, currentWidth + delta);

        _.set(vm, path, newWidth);

        const localStorageKey = mkColWidthStorageKey(vm.scenarioId);
        localStorageService.set(localStorageKey, vm.layoutOptions.colWidths);
    }

    function addApplicationAction(domainCoordinates) {
        const column = domainCoordinates.column;
        const row = domainCoordinates.row;

        const applicationPickList = determineApplicationPickList(
            vm.applications,
            vm.scenarioDefn.ratings,
            column,
            row);

        const style = mkDialogStyle();

        $timeout(() => {
            vm.selectedRow = row;
            vm.selectedColumn = column;
            vm.dialog = {
                type: dialogs.ADD_APPLICATION,
                row,
                column,
                applicationPickList,
                tabs: [
                    { name: "Pick app", value: "PICK" },
                    { name: "Search for any app", value: "SEARCH" }
                ],
                tab: "PICK",
                style
            };
        });
    }

    const switchToEditModeAction =  {
        title: "Switch to edit mode",
        action:  () => $timeout(() => vm.mode = modes.EDIT)
    };

    const addAction = mkAction(
        "Add another application",
        (elm, d) => addApplicationAction(d.domainCoordinates));

    const editAction = mkAction(
        "Edit",
        (elm, d) => {
            const style = mkDialogStyle();
            $timeout(() => {
                vm.dialog = {
                    type: dialogs.EDIT_CELL,
                    data: d,
                    workingState: Object.assign({ rating: "G", comment: "" }, d.state),
                    style
                };
            });
        });

    const removeAction = mkAction(
        "Remove",
        (elm, d) => {
            const removeRatingScenarioCommand = getChangeScenarioCommand(
                vm.scenarioDefn,
                d.node.id,
                d.domainCoordinates.column.id,
                d.domainCoordinates.row.id
            );
            serviceBroker
                .execute(
                    CORE_API.ScenarioStore.removeRating,
                    [removeRatingScenarioCommand])
                .then(() => notification.success("Item removed"))
                .then(() => reload());
        });


    function mkNodeMenu() {
        return () => {
            hideInfoPopup();
            if (!vm.permissions.edit) {
                return null;
            }

            if (vm.mode === modes.VIEW) {
                return [
                    switchToEditModeAction,
                ];
            } else {
                return [
                    addAction,
                    editAction,
                    divider,
                    removeAction
                ];
            }
        };
    }

    function mkNodeGridMenu() {
        return () => {
            hideInfoPopup();
            if (!vm.permissions.edit) {
                return null;
            }

            if (vm.mode === modes.VIEW) {
                return [ switchToEditModeAction ];
            } else {
                return [
                    {
                        title: "Add application",
                        action: (elm, d) => addApplicationAction({ row: d.row, column: d.column })
                    }
                ];
            }
        };
    }

    function mkAxisItemMenu() {
        return (data) => {
            const hide = {
                title: "Hide",
                action: (elm, d) => $timeout(() => {
                    if (vm.hiddenAxes.length === 0) {
                        notification.info("Axis has been removed from grid, you can restore from the 'Hidden Axes' menu in Diagram Controls");
                    }
                    vm.hiddenAxes.push(d);
                    vm.vizData = prepData();
                })
            };

            const contract =  {
                title: "Decrease width",
                action: (elm, d) => $timeout(() => {
                    alterColWidth(d.id, -1);
                    vm.vizData = prepData();
                })
            };

            const expand = {
                title: "Increase width",
                action: (elm, d) => $timeout(() => {
                    alterColWidth(d.id, 1);
                    vm.vizData = prepData();
                })
            };

            return _.compact([
                hide,
                data.axisOrientation === "COLUMN" ? contract : null,
                data.axisOrientation === 'COLUMN' ? expand : null
            ]);
        };
    }

    function reload() {
        loadApplications(loadScenario())
            .then(() => vm.vizData = prepData());
    }

    function loadScenario() {
        return serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.getById,
                [vm.scenarioId],
                { force: true })
            .then(r => vm.scenarioDefn = r.data);
    }

    function loadApplications(scenarioPromise) {
        return scenarioPromise
            .then(() => _.map(vm.scenarioDefn.ratings, r => r.item.id))
            .then(appIds => serviceBroker
                .loadViewData(
                    CORE_API.ApplicationStore.findByIds,
                    [ appIds ],
                    { force: true }))
            .then(r => vm.applications = r.data);
    }

    function loadMeasurables(scenarioPromise) {
        return scenarioPromise
            .then(() => serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll))
            .then(r => {
                const requiredMeasurableIds = _.map(vm.scenarioDefn.axisDefinitions, d => d.domainItem.id);
                vm.measurables = _.filter(r.data, m => _.includes(requiredMeasurableIds, m.id));
            });
    }

    function setupHandlers() {
        // create the div element that will hold the context menu
        selectAll(`.${styles.INFOPOP}`)
            .data([1])
            .enter()
            .append("div")
            .attr("class", styles.INFOPOP);

        return {
            onNodeClick: (d) => {
                const name = d.node.name;
                const column = d.domainCoordinates.column.name;
                const row = d.domainCoordinates.row.name;
                const ratingName = _.get(vm, ["ratingsByCode", d.state.rating, "name"], "?");
                const comment = _.get(d, ["state", "comment"], "- No comment -");
                const urlEnding = $state.href(kindToViewState( d.node.kind ), { id: d.node.id });
                const url = $window.location.origin + urlEnding;

                const html = `
                    <table class="table table-condensed small">
                        <thead>
                        <tr>
                            <th colspan="2">
                                <a href="${ url }">${ name }</a>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>Column:</td>
                            <td>${ column }</td>
                        </tr>
                        <tr>
                            <td>Row:</td>
                            <td>${ row }</td>
                        </tr>
                        <tr>
                            <td>Comment:</td>
                            <td>${ comment }</td>
                        </tr>
                        <tr>
                            <td>Rating:</td>
                            <td>${ ratingName }</td>
                        </tr>
                        </tbody>
                    </table>
                    <a class="clickable infopop-close">
                        Close
                    </a>
                `;

                showInfoPopup(html);
            },
            onNodeGridClick: () => {
                hideInfoPopup();
            },
            contextMenus: {
                node: mkNodeMenu(),
                nodeGrid: mkNodeGridMenu(),
                axisItem: mkAxisItemMenu()
            }
        };
    }

    vm.$onInit = () => {
        const scenarioPromise = loadScenario();
        const applicationPromise = loadApplications(scenarioPromise);
        const measurablePromise = loadMeasurables(scenarioPromise);

        scenarioPromise
            .then( () => serviceBroker.loadAppData(
                CORE_API.RatingSchemeStore.getById,
                [ vm.scenarioDefn.roadmap.ratingSchemeId ]))
            .then(r => vm.ratingsByCode = _.keyBy(r.data.ratings, "rating"));

        $q.all([scenarioPromise, applicationPromise, measurablePromise])
            .then(() => {
                vm.vizData = prepData();
                const cols = vm.vizData.columnHeadings;
                const colWidths = localStorageService.get(mkColWidthStorageKey(vm.scenarioId)) || {};

                vm.layoutOptions = mkLayoutOptions(
                    cols,
                    colWidths);

            });

        userService
            .whoami()
            .then(u => vm.permissions = {
                admin: userService.hasRole(u, roles.SCENARIO_ADMIN),
                edit: userService.hasRole(u, roles.SCENARIO_EDITOR)
            });

        vm.handlers = setupHandlers();
    };

    const getLastOrDefaultRating = (app) => {
        return vm.lastRatings[app.id] || getDefaultRating(_.values(vm.ratingsByCode));
    };


    // -- INTERACT --

    vm.toggleMode = () => {
        vm.mode = vm.mode === modes.VIEW
            ? modes.EDIT
            : modes.VIEW;
    };

    vm.onRatingSelect = (rating) => {
        vm.dialog.workingState.rating = rating;
    };

    vm.onAddApplication = (app) => {
        const lastOrDefaultRating = getLastOrDefaultRating(app);

        const addRatingScenarioCommand = getChangeScenarioCommand(
            vm.scenarioDefn,
            app.id,
            vm.selectedColumn.id,
            vm.selectedRow.id,
            lastOrDefaultRating);

        serviceBroker
            .execute(
                CORE_API.ScenarioStore.addRating,
                [addRatingScenarioCommand])
            .then(() => {
                _.remove(vm.dialog.applicationPickList, a => a.id === app.id);
                reload();
                notification.success("Added rating");
            });
    };

    vm.onSaveCell =(item, column, row, workingState, currentRating) => {
        const changeScenarioCommand = getChangeScenarioCommand(
            vm.scenarioDefn,
            item.id,
            column.id,
            row.id,
            workingState.rating,
            workingState.comment,
            currentRating);

        serviceBroker
            .execute(
                CORE_API.ScenarioStore.updateRating,
                [changeScenarioCommand])
            .then(() => {
                reload();
                notification.success("Edited rating");
                vm.onCloseDialog();
                vm.lastRatings[item.id] = workingState.rating;
            });
    };

    vm.onCloseDialog = () => {
        vm.dialog = null;
    };

    vm.unhideAxis = (axis) => {
        _.remove(vm.hiddenAxes, (d => d.id === axis.id));
        vm.vizData = prepData();
    };

    vm.unhideAllAxes = () => {
        vm.hiddenAxes = [];
        vm.vizData = prepData();
    };
}


controller.$inject = [
    "$q",
    "$timeout",
    "localStorageService",
    "Notification",
    "ServiceBroker",
    "UserService",
    "$window",
    "$state"
];


const id = "waltzRoadmapScenarioDiagram";


export default {
    id,
    component
};


