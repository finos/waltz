import template from "./svelte-page.html";
import Sidebar from "./Sidebar.svelte";
import {sidebarExpanded, sidebarVisible} from "./sidebar-store";
import Toasts from "../notification/components/toaster/Toasts.svelte";
import ToastStore from "../svelte-stores/toast-store"
import Popover from "../common/svelte/popover/Popover.svelte";
import {isIE} from "../common/browser-utils";

function controller($scope, $timeout, settingsService, $rootScope) {
    const vm = this;

    vm.Sidebar = Sidebar;
    vm.Toasts = Toasts;
    vm.Popover = Popover;

    const unsubExpand = sidebarExpanded.subscribe((d) => {
        $scope.$applyAsync(() => vm.isExpanded = d);
        $timeout(() => {}, 100); // nudging angular so things like ui-grid can auto resize
    });

    const unsubVisible = sidebarVisible.subscribe((d) => {
        $scope.$applyAsync(() => vm.isVisible = d);
    });

    vm.$onDestroy = () => {
        unsubExpand();
        unsubVisible();
    };


    vm.$onInit = () => {
        if (isIE()) {
            settingsService
                .findOrDefault(
                    "ui.banner.message",
                    "Waltz is optimised for use in modern browsers. For example Google Chrome, Firefox and Microsoft Edge")
                .then(m => ToastStore.info(m, {timeout: 10000}));
        }
    }
}

controller.$inject = [
    "$scope",
    "$timeout",
    "SettingsService",
    "$rootScope"
];

const component = {
    template,
    controller,
    bindings: {}
};

export default {
    id: "waltzSveltePage",
    component
}