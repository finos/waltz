import template from "./svelte-page.html";
import Sidebar from "./Sidebar.svelte";
import {sidebarExpanded, sidebarVisible} from "./sidebar-store";
import Toasts from "../notification/components/toaster/Toasts.svelte";

function controller($scope, $timeout) {
    const vm = this;

    vm.Sidebar = Sidebar;
    vm.Toasts = Toasts;

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
}

controller.$inject = ["$scope", "$timeout"];

const component = {
    template,
    controller,
    bindings: {}
};

export default {
    id: "waltzSveltePage",
    component
}