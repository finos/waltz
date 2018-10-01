import template from "./roadmap-scenario-diagram.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {prepareData} from "../../../scenario/components/scenario-diagram/scenario-diagram-data-utils";
import {initialiseData} from "../../../common";
import {event} from "d3-selection";

const bindings = {
    scenarioId: "<",
    onCancel: "<"
};


const dialogs = {
    ADD_APPLICATION: "ADD_APPLICATION",
    EDIT_CELL: "EDIT_CELL"
};


const component = {
    bindings,
    template,
    controller
};


const initialState = {
    handlers: {
        onNodeClick: (n) => console.log("WRSD: NodeClick", n)
    },
    dialog: null
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


function controller($q, $timeout, serviceBroker, notification) {

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

    function mkNodeMenu() {
        return () => {
            return [
                {
                    title: "Add another application",
                    action:  (elm, d) => addApplicationAction(d.domainCoordinates)
                }, {
                    title: "Edit",
                    action: (elm, d) => {
                        const style = mkDialogStyle();
                        $timeout(() => {
                            const row = d.domainCoordinates.row;
                            const column = d.domainCoordinates.column;

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
        };
    }

    function mkNodeGridMenu() {
        return () => {
            return [
                {
                    title: "Add application",
                    action:  (elm, d) => addApplicationAction({ row: d.row, column: d.column })
                }
            ];
        };
    }

    function reload() {
        loadApplications(loadScenario())
            .then(() => vm.vizData =
                prepareData(
                    vm.scenarioDefn,
                    vm.applications,
                    vm.measurables));
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
                const requiredMeasurableIds = _.map(vm.scenarioDefn.axisDefinitions, d => d.item.id);
                vm.measurables = _.filter(r.data, m => _.includes(requiredMeasurableIds, m.id));
            });
    }

    vm.$onInit = () => {
        const scenarioPromise = loadScenario();
        const applicationPromise = loadApplications(scenarioPromise);
        const measurablePromise = loadMeasurables(scenarioPromise);

        $q.all([scenarioPromise, applicationPromise, measurablePromise])
            .then(() => vm.vizData = prepareData(
                vm.scenarioDefn,
                vm.applications,
                vm.measurables));

        vm.handlers.contextMenus = {
            node: mkNodeMenu(),
            nodeGrid: mkNodeGridMenu(),
            columnAxisItem: mkNodeMenu(),
            rowAxisItem: mkNodeMenu(),
        };
    };


    // -- INTERACT --

    vm.onRatingSelect = (rating) => {
        vm.dialog.workingState.rating = rating;
    };

    vm.onAddApplication = (app) => {
        const args = [
            vm.scenarioDefn.scenario.id,
            app.id,
            vm.selectedColumn.id,
            vm.selectedRow.id,
            "G"
        ];
        serviceBroker
            .execute(
                CORE_API.ScenarioStore.addRating,
                args)
            .then(() => {
                reload();
                notification.success("Added rating");
                vm.onCloseDialog();
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
            });
    };

    vm.onCloseDialog = () => {
        vm.dialog = null;
    };
}


controller.$inject = [
    "$q",
    "$timeout",
    "ServiceBroker",
    "Notification"
];


const id = "waltzRoadmapScenarioDiagram";


export default {
    id,
    component
};


