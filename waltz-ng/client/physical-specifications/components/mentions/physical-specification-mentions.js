import {initialiseData, mkEntityLinkGridCell, mkLinkGridCell} from "../../../common";

const bindings = {
    mentions: '<',
    onInitialise: '<'
};


const initialState = {
    mentions: [],
    onInitialise: (e) => {}
};


const template = require('./physical-specification-mentions.html');


function controller() {

    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        Object.assign(mkLinkGridCell('Specification', 'specification.name', 'flow.id', 'main.physical-flow.view'), { width: "20%"} ),
        Object.assign(mkEntityLinkGridCell('From', 'sourceEntity', 'left'), { width: "13%" }),
        Object.assign(mkEntityLinkGridCell('To', 'targetEntity', 'left'), { width: "13%" }),
        { field: 'specification.format', displayName: 'Format', width: "10%" },
        { field: 'flow.transport', displayName: 'Transport', width: "12%" },
        { field: 'flow.frequency', displayName: 'Frequency', width: "10%" },
        { field: 'specification.description', displayName: 'Description', width: "22%" }
    ];

    vm.onGridInitialise = (e) => {
        vm.exportFn = e.exportFn;
    };

    vm.exportGrid = () => {
        vm.exportFn('mentions.csv');
    };

    // callback
    vm.onInitialise({
        exportFn: vm.exportGrid
    });

}


const component = {
    template,
    bindings,
    controller
};


export default component;