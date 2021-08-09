import template from "./svelte-page.html";
import Sidebar from "./Sidebar.svelte";
import {sidebarExpanded, sidebarVisible} from "./sidebar-store";

function controller($scope) {
    const vm = this;
    vm.Sidebar = Sidebar;
    sidebarExpanded.subscribe((d) => $scope.$applyAsync(() => vm.isExpanded = d));
    sidebarVisible.subscribe((d) => $scope.$applyAsync(() => vm.isVisible = d));
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