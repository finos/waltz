import template from "./svelte-page.html";
import Sidebar from "./Sidebar.svelte";
import {sidebarExpanded, sidebarVisible} from "./sidebar-store";

function controller($scope) {
    const vm = this;

    vm.Sidebar = Sidebar;
    const unsubExpand = sidebarExpanded.subscribe((d) => $scope.$applyAsync(() => vm.isExpanded = d));
    const unsubVisible = sidebarVisible.subscribe((d) => $scope.$applyAsync(() => vm.isVisible = d));

    vm.$onDestroy = () => {
        unsubExpand();
        unsubVisible();
    };
}

controller.$inject = ["$scope"];

const component = {
    template,
    controller,
    bindings: {}
};

export default {
    id: "waltzSveltePage",
    component
}