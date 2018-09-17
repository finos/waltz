import ListView from "./pages/list/measurable-category-list.js";


const baseState = {
};


const listState = {
    url: "measurable-category/{id:int}",
    views: {
        "content@": ListView
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.measurable-category", baseState)
        .state("main.measurable-category.list", listState);
}


setup.$inject = [
    "$stateProvider"
];


export default setup;