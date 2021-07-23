import template from "./svelte-page.html";
import Sidebar from "./Sidebar.svelte";

function controller() {
    const vm = this;
    vm.Sidebar = Sidebar;
}

const component = {
    template,
    controller,
    bindings: {}
};

export default {
    id: "waltzSveltePage",
    component
}