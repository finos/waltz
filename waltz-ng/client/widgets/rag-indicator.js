const BINDINGS = {
    name: '@',
    rating: '=ragRating'
};


function controller() {
    const vm = this;

    vm.ragToTextColor = (rag) => {
        switch (rag) {
            case 'G':
                return "text-success";
            case 'A':
                return "text-warning";
            case 'R':
                return "text-danger";
            default:
                return "text-info";
        }
    }
}

controller.$inject = [];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./rag-indicator.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
