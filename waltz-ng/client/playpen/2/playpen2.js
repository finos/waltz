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
                    physicalSpecificationStore) {

    const vm = Object.assign(this, initData);

    vm.sourceEntity = {
        id: 86,
        name: 'Source App',
        kind: 'APPLICATION'
    }

    // vm.specification = {
    //     id: 2,
    //     name: 'sample spec',
    //     description: 'description about the spec',
    //     format: 'BINARY'
    // }


    // vm.targetEntity = {
    //     id: 2,
    //     name: 'Target App',
    //     kind: 'ACTOR'
    // }

    // vm.flowAttributes = {
    //     basis: '+1',
    //     transport: 'FILE_TRANSPORT',
    //     frequency: 'DAILY' };

    vm.flowAttributes = {
        basis: '0',
        transport: '',
        frequency: '' };



    vm.focusSpecification = () => {
        vm.visibility.editor = 'SPECIFICATION';
    };

    vm.focusFlowAttributes = () => {
        vm.visibility.editor = 'FLOW-ATTRIBUTES';
    };

    vm.focusTarget = () => {
        vm.visibility.editor = 'TARGET-ENTITY';
    };


    vm.attributesChanged = (attributes) => {
        vm.flowAttributes = attributes;
        vm.editorDismiss();
    };



    vm.targetChanged = (target) => {
        vm.targetEntity = target;
        vm.editorDismiss();
    };

    vm.editorDismiss = () => {
        vm.visibility.editor = '';
    }

    physicalSpecificationStore
        .findByEntityReference(vm.sourceEntity)
        .then(xs => vm.candidateSpecifications = xs);

    vm.changeSpecification = (spec) => {
        notification.info("Specification selected: " + spec.name);
        vm.specification = spec;
        vm.editorDismiss();
    };

}


controller.$inject = [
    'Notification',
    'PhysicalSpecificationStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
