
const initData = {
    candidateSpecifications: [],
    visible: true,
    current: null
};


function controller($q,
                    $scope,
                    notification,
                    physicalSpecificationStore) {
    const vm = Object.assign(this, initData);

    const appId = 66779;
    const sourceEntity = {
        kind: 'APPLICATION',
        id: appId
    };

    vm.sourceEntity = sourceEntity;

    physicalSpecificationStore
        .findByEntityReference(sourceEntity)
        .then(xs => vm.candidateSpecifications = xs);

    vm.changeSpecification = (spec) => {
        notification.info("Specification selected: " + spec.name);
        vm.current = spec;
        vm.cancel();
    };

    vm.cancel = () => vm.visible = false;

}


controller.$inject = [
    '$q',
    '$scope',
    'Notification',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;