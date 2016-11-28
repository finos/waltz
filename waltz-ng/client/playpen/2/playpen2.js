const initData = {
    gridData: [],
    filteredGriData: [],
    flowAttributeEditorVisible: false,
    targetEntityPickerVisible: false,
    visibility: {
        editor: '' // FLOW-ATTRIBUTES, TARGET-ENTITY, SPECIFICATION
    }
};


function controller(notification,
                    store) {

    const vm = Object.assign(this, initData);


    store
        .findByAppId(67672)
        .then(x => {
            console.log(x);
            return x;
        })
        .then(r => vm.databases = r);
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
