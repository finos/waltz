import ProposeDataFlowView from "./components/data-flow-section/proposed-data-flow/propose-data-flow";
const baseState = {
    url: "data-flow"
};

const createState = {
    url: "/create/{kind:string}/{id:int}",
    views: {"content@": ProposeDataFlowView }
};

function setup($stateProvider) {
    $stateProvider
        .state("main.data-flow", baseState)
        .state("main.data-flow.create", createState);
}

setup.$inject = [
    "$stateProvider"
];

export default setup;