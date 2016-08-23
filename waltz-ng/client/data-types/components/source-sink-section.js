import {initialiseData} from "../../common";


const bindings = {
    ratings: '<',
    sourceSinks: '<',
};


const template = require('./source-sink-section.html');


const initialState = {
    ratings: [],
    sourceSinks: null,
    visibility: {
        sourcesOverlay: false
    }
};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [
];


const component = {
    bindings,
    controller,
    template
};


export default component;