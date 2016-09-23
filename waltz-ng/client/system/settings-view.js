function controller(settingsService) {

    const vm = this;

    vm.settings = [];

    const load = (force = false) => settingsService
        .findAll(force)
        .then(settings => vm.settings = settings);

    vm.forceRefresh = () => {
        load(true);
    };

    load();

}

controller.$inject = [ 'SettingsService' ];


export default {
    template: require('./settings-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};