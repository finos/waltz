import RoadmapView from "./pages/view/roadmap-view";


const baseState = {
};


const viewState = {
    url: "roadmap/{id:int}",
    views: {
        "content@": RoadmapView.component
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.roadmap", baseState)
        .state("main.roadmap.view", viewState);
}

setup.$inject = [
    "$stateProvider"
];


export default setup;