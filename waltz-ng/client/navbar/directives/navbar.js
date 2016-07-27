import _ from "lodash";


const initialState = {
    logoOverlayText: '',
    logoOverlayColor: '#444',
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
                    $uibModal,

                    settingsStore,
                    userService) {

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



    const reloadPage = () => $state.reload();

    const rejected = () => alert('Invalid username/password');

    const logout = () => userService
        .logout()
        .then(reloadPage);


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
    '$uibModal',
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
