import _ from "lodash";


const initialState = {
    history: [],
    logoOverlayText: '',
    logoOverlayColor: '#444',
    query: '',
    searchResults: {
        show: false,
        apps: [],
        people: [],
        capabilities: [],
        orgUnits: []
    },
    user: null
};


function loginController($scope, $uibModalInstance, logoOverlayText) {
    $scope.ok = () => {
        const credentials = {
            userName: $scope.username,
            password: $scope.password
        };
        $uibModalInstance.close(credentials);
    };

    $scope.username = '';
    $scope.password = '';
    $scope.logoOverlayText = logoOverlayText || '';

    $scope.cancel = () => $uibModalInstance.dismiss('cancel');
}

loginController.$inject = [
    '$scope',
    '$uibModalInstance',
    'logoOverlayText'
];


function controller($scope,
                    $state,
                    $timeout,
                    $uibModal,
                    applicationStore,
                    capabilityStore,
                    localStorageService,
                    personStore,
                    orgUnitStore,
                    settingsStore,
                    userService) {

    const searchResults = {
        show: false
    };

    const vm = _.defaultsDeep(this, initialState);

    settingsStore
        .findAll()
        .then(settings => {
            vm.logoOverlayText = settingsStore.findOrDefault(settings, "ui.logo.overlay.text", "");
            vm.logoOverlayColor = settingsStore.findOrDefault(settings, "ui.logo.overlay.color", "");
            vm.allowDirectLogin = settingsStore.findOrDefault(settings, 'web.authentication', "") === 'waltz';
        });

    userService
        .whoami(true) // force
        .then(user => vm.user = user);

    const doSearch = (query) => {
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
    };

    const reloadPage = () => $state.reload();

    const rejected = () => alert('Invalid username/password');

    const logout = () => userService
        .logout()
        .then(reloadPage);

    const dismissResults = () => $timeout(() => { searchResults.show = false; }, 400);


    vm.searchResults = searchResults;
    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => searchResults.show;
    vm.dismissResults = dismissResults;
    vm.refreshHistory = () => vm.history = localStorageService.get('history_2') || [];
    vm.logout = logout;
    vm.login = () => {

        var loginModalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'navbar/modal-login.html',
            controller: loginController,
            resolve: {
                logoOverlayText: () => vm.logoOverlayText
            },
            size: 'sm'
        });

        loginModalInstance.result
            .then(
                (credentials) => userService
                    .login(credentials)
                    .then(reloadPage, rejected),
                () => console.log('Login dismissed at: ' + new Date()));

    };
}


controller.$inject = [
    '$scope',
    '$state',
    '$timeout',
    '$uibModal',
    'ApplicationStore',
    'CapabilityStore',
    'localStorageService',
    'PersonStore',
    'OrgUnitStore',
    'SettingsStore',
    'UserService'
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
