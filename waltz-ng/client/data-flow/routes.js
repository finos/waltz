import ProposeDataFlowView from "./components/data-flow-section/proposed-data-flow/propose-data-flow";
import UserDashboardView from "./components/user-dashboard/user-dashboard-view";

const baseState = {
    url: "data-flow"
};

const createState = {
    url: "/create/{kind:string}/{id:int}?{targetLogicalFlowId:int}",
    views: { "content@": ProposeDataFlowView }
};

const userDashboardState = {
    url: "/dashboard/user",
    views: {"content@": UserDashboardView}
}

function setup($stateProvider) {
    $stateProvider
        .state("main.data-flow", baseState)
        .state("main.data-flow.create", createState)
        .state("main.data-flow.dashboard", userDashboardState);
}

setup.$inject = [
    "$stateProvider"
];

export default setup;