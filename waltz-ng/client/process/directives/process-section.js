const BINDINGS = {
    processes: "=",
    sourceDataRatings: '='
};


const initialState = {
    processes: [],
    sourceDataRatings: []
};



function controller() {
    Object.assign(this, initialState);
}



const directive = {
    restrict: 'E',
    replace: true,
    template: require('./process-section.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;

