import {initialiseData} from "../../common";


const initialState = {
    logoOverlayText: '',
    logoOverlayColor: '#444',
};


function controller(settingsStore) {
    const vm = initialiseData(this, initialState);

    settingsStore
        .findAll()
        .then(settings => {
            vm.logoOverlayText = settingsStore.findOrDefault(settings, "ui.logo.overlay.text", "");
            vm.logoOverlayColor = settingsStore.findOrDefault(settings, "ui.logo.overlay.color", "");
        });


}


controller.$inject = ['SettingsStore',];


export default () => {
    return {
        restrict: 'E',
        template: require("./navbar.html"),
        controller,
        scope: {},
        controllerAs: 'ctrl'
    };
};
