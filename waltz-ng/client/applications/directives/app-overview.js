function controller($state) {
    const vm = this;
    vm.tagSelected = (tag) => {
        $state.go('main.app.tag-explorer', { tag });
    };
}

controller.$inject = ['$state'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: {
        app: '=',
        tags: '=',
        aliases: '=',
        organisationalUnit: '='
    },
    controller,
    controllerAs: 'ctrl',
    template: require('./app-overview.html')
});
