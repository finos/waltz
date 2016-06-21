import _ from "lodash";


function controller($timeout,
                    applicationStore,
                    capabilityStore,
                    personStore,
                    orgUnitStore,
                    settingsStore) {
    const searchResults = {
        show: false
    };

    settingsStore
        .findAll()
        .then(settings => {
            vm.logoOverlayText = settingsStore.findOrDefault(settings, "ui.logo.overlay.text", "");
            vm.logoOverlayColor = settingsStore.findOrDefault(settings, "ui.logo.overlay.color", "");
        });

    function doSearch(query) {
        if (_.isEmpty(query)) {
            searchResults.show = false;
        } else {
            searchResults.show = true;
            applicationStore
                .search(query)
                .then(r => searchResults.apps = r);
            personStore
                .search(query)
                .then(r => searchResults.people = r);
            capabilityStore
                .search(query)
                .then(r => searchResults.capabilities = r);
            orgUnitStore
                .search(query)
                .then(r => searchResults.orgUnits = r);
        }
    }

    function dismissResults() {
        $timeout(() => { searchResults.show = false; }, 400);
    }

    const vm = this;
    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => searchResults.show;
    vm.dismissResults = dismissResults;
    vm.searchResults = searchResults;

}


controller.$inject = [
    '$timeout',
    'ApplicationStore',
    'CapabilityStore',
    'PersonStore',
    'OrgUnitStore',
    'SettingsStore'
];


export default () => {
    return {
        restrict: 'E',
        template: require("./navbar.html"),
        controller,
        scope: {},
        controllerAs: 'ctrl'
    };
};
