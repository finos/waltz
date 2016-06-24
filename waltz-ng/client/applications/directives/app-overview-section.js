function controller() {
    const vm = this;
}

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: {
        app: '=',
        organisationalUnit: '=',
        complexity: '=',
        tags: '=',
        aliases: '=',
        updateAliases: '&'
    },
    controller,
    controllerAs: 'ctrl',
    template: require('./app-overview-section.html')
});
