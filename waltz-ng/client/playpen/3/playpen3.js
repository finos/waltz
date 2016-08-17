
function controller() {

}


controller.$inject = [
    '$element',
    '$q',
    'AppCapabilityStore',
    'CapabilityStore'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;