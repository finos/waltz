import {initialiseData} from '../../../common';


const bindings = {
    physicalFlows: '<'
};


const initialState = {
    physicalFlows: [{
        specification: {
            name: "speccy",
            id: 2,
            description: "about speccy",
            owningEntity: {
                name: 'appA',
                id: 15,
                kind: 'APPLICATION'
            }
        },
        targetEntity: {
            kind: 'APPLICATION',
            id: 12,
            name: "appB"
        }
    }]
};


const template = require('./physical-specification-mentions.html');


function controller() {
    const vm = initialiseData(this, initialState);
    console.log("TODO: physical specification mentions")
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;