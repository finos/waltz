function controller() {

}

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
    template: require('./app-overview-section.html')
});
