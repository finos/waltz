import template from "./physical-flow-overview-node.html";


const bindings = {
    criticalityMismatch: "<?",
    node: "<",
    participants: "<"
};


const component = {
    bindings,
    template
};


export default {
    id: "waltzPhysicalFlowOverviewNode",
    component
};

