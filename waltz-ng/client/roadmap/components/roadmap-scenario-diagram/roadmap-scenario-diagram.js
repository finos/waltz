import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {prepareData} from "../../../scenario/components/scenario-diagram/scenario-diagram-data-utils";
import {initialiseData} from "../../../common";
import {event, select, selectAll} from "d3-selection";
import roles from "../../../user/roles";

import template from "./roadmap-scenario-diagram.html";


const bindings = {
    scenarioId: "<",
    onCancel: "<"
};

const modes = {
    EDIT: "EDIT",
    VIEW: "VIEW"
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


const initialState = {
    dialog: null,
    handlers: {},
    hiddenAxes: [],
    lastRatings: {},
    layoutOptions: {
        defaultColMaxWidth: 2,
        maxColWidths: {}, // { domainItemId: width }
    },
    mode: modes.VIEW,
    permissions: {
        admin: false,
        edit: false
    }
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


function controller($q,
                    $timeout,
                    notification,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initialState);

    const mkDialogStyle = () => ({
        display: "block",
        position: "absolute",
        left: `${event.pageX - 2}px`,
        top:`${event.pageY - 100}px`
    });

    const addApplicationAction = (domainCoordinates) => {
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
    };

    const switchToEditModeAction =  {
        title: "Switch to edit mode",
        action:  () => $timeout(() => vm.mode = modes.EDIT)
    };


    function prepData() {
        return prepareData(vm.scenarioDefn, vm.applications, vm.measurables, vm.hiddenAxes);
    }


    function mkNodeMenu() {
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
                        title: "Add another application",
                        action:  (elm, d) => addApplicationAction(d.domainCoordinates)
                    }, {
                        title: "Edit",
                        action: (elm, d) => {
                            const style = mkDialogStyle();
                            $timeout(() => {
                                vm.dialog = {
                                    type: dialogs.EDIT_CELL,
                                    data: d,
                                    workingState: Object.assign({ rating: "G", comment: "" }, d.state),
                                    style
                                };
                            });
                        }
                    },  {
                        divider: true
                    }, {
                        title: "Remove",
                        action: (elm, d) => {
                            const args = [
                                vm.scenarioDefn.scenario.id,
                                d.node.id,
                                d.domainCoordinates.column.id,
                                d.domainCoordinates.row.id
                            ];
                            serviceBroker
                                .execute(
                                    CORE_API.ScenarioStore.removeRating,
                                    args)
                                .then(() => notification.success("Item removed"))
                                .then(() => reload());
                        }
                    }
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
        return (data, elem, index) => {
            const hide = {
                title: "Hide",
                action: (elm, d) => $timeout(() => {
                    if(vm.hiddenAxes.length === 0) {
                        notification.info("Hid axis from grid, you can restore from the Hidden Axes menu in Diagram Controls");
                    }
                    vm.hiddenAxes.push(d);
                    vm.vizData = prepData();
                })
            };

            const contract =  {
                title: "Decrease width",
                action: (elm, d) => $timeout(() => {
                    const newMaxColWidth = (vm.layoutOptions.maxColWidths[d.id] || 0) - 1;
                    vm.layoutOptions.maxColWidths[d.id] = Math.max(newMaxColWidth, vm.layoutOptions.defaultColMaxWidth);
                    vm.vizData = prepData();
                })
            };

            const expand = {
                title: "Increase width",
                action: (elm, d) => $timeout(() => {
                    vm.layoutOptions.maxColWidths[d.id] = (vm.layoutOptions.maxColWidths[d.id] || vm.layoutOptions.defaultColMaxWidth) + 1;
                    vm.vizData = prepData();
                })
            };

            return _.compact([
                hide,
                data.axisOrientation === 'COLUMN' ? contract : null,
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

                const html = `
                    <table class="table table-condensed small">
                        <thead>
                        <tr>
                            <th colspan="2">${ name }</th>
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
            .then(() => vm.vizData = prepData());

        vm.handlers = setupHandlers();

        userService
            .whoami()
            .then(u => vm.permissions = {
                admin: userService.hasRole(u, roles.SCENARIO_ADMIN),
                edit: userService.hasRole(u, roles.SCENARIO_EDITOR)
            });
    };


    const getLastOrDefaultRating = (app) => {
        const getDefaultRating = () => _.chain(vm.ratingsByCode)
            .values()
            .sortBy(["position"])
            .head()
            .value();
        return _.get(vm.lastRatings, [app.id], getDefaultRating())
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

        const args = [
            vm.scenarioDefn.scenario.id,
            app.id,
            vm.selectedColumn.id,
            vm.selectedRow.id,
            lastOrDefaultRating
        ];
        serviceBroker
            .execute(
                CORE_API.ScenarioStore.addRating,
                args)
            .then(() => {
                _.remove(vm.dialog.applicationPickList, a => a.id === app.id);
                reload();
                notification.success("Added rating");
            });
    };

    vm.onSaveCell =(item, column, row, workingState) => {
        const args = [
            vm.scenarioDefn.scenario.id,
            item.id,
            column.id,
            row.id,
            workingState.rating,
            workingState.comment
        ];

        serviceBroker
            .execute(
                CORE_API.ScenarioStore.updateRating,
                args)
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
    "Notification",
    "ServiceBroker",
    "UserService"
];


const id = "waltzRoadmapScenarioDiagram";


export default {
    id,
    component
};


