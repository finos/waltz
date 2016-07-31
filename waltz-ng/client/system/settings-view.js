function controller(settingsStore) {

    const vm = this;

    vm.settings = [];

    const load = (force = false) => settingsStore
        .findAll(force)
        .then(settings => vm.settings = settings);

    vm.forceRefresh = () => {
        load(true);
    };

    load();

}

controller.$inject = [ 'SettingsStore' ];


export default {
    template: require('./settings-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};