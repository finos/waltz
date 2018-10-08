import ScenarioView from "./pages/view/scenario-view";


const baseState = {
};


const viewState = {
    url: "scenario/{id:int}",
    views: {
        "content@": ScenarioView.component
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.scenario", baseState)
        .state("main.scenario.view", viewState);
}

setup.$inject = [
    "$stateProvider"
];


export default setup;