import RoadmapView from "./pages/view/roadmap-view";
import RoadmapList from "./pages/list/roadmap-list";


const baseState = {
};


const viewState = {
    url: "roadmap/{id:int}",
    views: {
        "content@": RoadmapView.id
    }
};


const listState = {
    url: "roadmap",
    views: {
        "content@": RoadmapList.id
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.roadmap", baseState)
        .state("main.roadmap.list", listState)
        .state("main.roadmap.view", viewState);
}

setup.$inject = [
    "$stateProvider"
];


export default setup;