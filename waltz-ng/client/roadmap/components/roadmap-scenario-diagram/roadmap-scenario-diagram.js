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
    ADD_APPLICATION: "ADD_APPLICATION"
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


function controller($q, $timeout, serviceBroker, notification) {


    function mkNodeMenu() {
        return (d) => {
            return [
                {
                    title: "Update rating",
                    action: (elm, d, i) => {
                        $timeout(() => {
                            console.log("Update rating")
                        });
                    }
                },  {
                    title: "Add comment",
                    action: (elm, d, i) => {
                        $timeout(() => {
                            console.log("Add comment")
                        });
                    }
                }, {
                    divider: true
                }, {
                    title: "Remove",
                    action: (elm, d, i) => {
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
                },
            ];
        };
    }

    function mkNodeGridMenu() {
        return (d) => {
            return [
                {
                    title: "Add Application",
                    action: (elm, d, i) => {
                        const style = {
                            display: "block",
                            position: "absolute",
                            //"z-index": 1200,
                            left: (event.pageX - 2) + "px",
                            top: (event.pageY - 100) + "px"
                        };

                        $timeout(() => {
                            vm.selectedRow = d.row;
                            vm.selectedColumn = d.column;
                            vm.dialog = {
                                type: dialogs.ADD_APPLICATION,
                                row: d.row,
                                column: d.column,
                                style
                            };
                        });
                    }
                }
            ];
        };
    }

    const vm = initialiseData(this, initialState);

    function reload() {
        loadScenario()
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
            .then(appIds => serviceBroker.loadViewData(CORE_API.ApplicationStore.findByIds, [appIds]))
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


    vm.onAddApplication = (app) => {
        const args = [
            vm.scenarioDefn.scenario.id,
            app.id,
            vm.selectedColumn.id,
            vm.selectedRow.id,
            "G"
        ];
        console.log({args});
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
}


