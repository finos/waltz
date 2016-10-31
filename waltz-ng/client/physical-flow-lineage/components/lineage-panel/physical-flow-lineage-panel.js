import {initialiseData, mkEntityLinkGridCell, mkLinkGridCell} from "../../../common";


const template = require('./physical-flow-lineage-panel.html');


const bindings = {
    lineage: '<',
    onInitialise: '<'
};


const initialState = {
    lineage: [],
    onInitialise: (e) => {}
};


function controller() {

    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        Object.assign(mkLinkGridCell('Specification', 'specification.name', 'flow.id', 'main.physical-flow.view'), { width: "20%"} ),
        Object.assign(mkEntityLinkGridCell('From', 'sourceEntity', 'left'), { width: "15%" }),
        Object.assign(mkEntityLinkGridCell('To', 'targetEntity', 'left'), { width: "15%" }),
        { field: 'specification.format', displayName: 'Format', width: "12%" },
        { field: 'flow.transport', displayName: 'Transport', width: "12%" },
        { field: 'flow.description', displayName: 'Description', width: "26%" }
    ];

    vm.onGridInitialise = (e) => {
        vm.exportFn = e.exportFn;
    };

    vm.exportGrid = () => {
        vm.exportFn('lineage.csv');
    };

    // callback
    vm.onInitialise({
        exportFn: vm.exportGrid
    });

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;