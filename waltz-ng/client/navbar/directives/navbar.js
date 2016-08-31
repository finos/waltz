import _ from "lodash";
import {initialiseData} from "../../common";


const navItems = [
    { uiSref: 'main.org-unit.list', icon: 'sitemap', displayName: 'Org Units' },
    { uiSref: 'main.capability.list', icon: 'puzzle-piece', displayName: 'Capabilities' },
    { uiSref: 'main.process.list', icon: 'code-fork', displayName: 'Processes' },
    { uiSref: 'main.person', icon: 'users', displayName: 'People' },
    { uiSref: 'main.data-type.list', icon: 'qrcode', displayName: 'Data Types', role: 'BETA_TESTER' }
];


const initialState = {
    logoOverlayText: '',
    logoOverlayColor: '#444',
    navItemsForRole: []
};


function getNavItemsFilteredByRole(userService, user, navItems) {
    return _.filter(navItems, i => i.role ? userService.hasRole(user, i.role) : true );
}


function controller(settingsStore, userService) {
    const vm = initialiseData(this, initialState);

    settingsStore
        .findAll()
        .then(settings => {
            vm.logoOverlayText = settingsStore.findOrDefault(settings, "ui.logo.overlay.text", "");
            vm.logoOverlayColor = settingsStore.findOrDefault(settings, "ui.logo.overlay.color", "");
        });


    userService
        .whoami()
        .then(user => {
            vm.navItemsForRole = getNavItemsFilteredByRole(userService, user, navItems);
        });

}


controller.$inject = ['SettingsStore', 'UserService'];


export default () => {
    return {
        restrict: 'E',
        template: require("./navbar.html"),
        controller,
        scope: {},
        controllerAs: 'ctrl'
    };
};
