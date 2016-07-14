


const initData = {

};



function controller($q) {

    const vm = Object.assign(this, initData);

}


controller.$inject = [
    '$q'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;