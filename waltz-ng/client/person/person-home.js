import angular from "angular";


const initialState = {
    person: null
};


function controller($scope,
                    $state,
                    staticPanelStore,
                    svgStore) {

    const vm = Object.assign(this, initialState);

    $scope.$watch('ctrl.person', (person) => {
        if (person) {
            const navKey = { empId: person.employeeId };
            $state.go('main.person.view', navKey);
        }
    });

    svgStore
        .findByKind('ORG_TREE')
        .then(xs => vm.diagrams = xs);

    staticPanelStore
        .findByGroup("HOME.PERSON")
        .then(panels => vm.panels = panels);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.person.view', { empId: b.value });
        angular.element(b.block).addClass('clickable');
    };

}


controller.$inject = [
    '$scope',
    '$state',
    'StaticPanelStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./person-home.html'),
    controllerAs: 'ctrl',
    controller
};


export default view;
