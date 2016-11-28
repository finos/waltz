const initData = {
    rating: 'R'
};


function controller(notification,
                    store) {

    const vm = Object.assign(this, initData);

    vm.onSelect = (r) => {
        console.log("r'", r);
        vm.rating = r;
    };


}


controller.$inject = [
    'Notification',
    'DatabaseStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
