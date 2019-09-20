import {kindToViewState} from "../common/link-utils";


function goToNotFound($state) {
    $state.go("main.entity.not-found", {}, {location: false});
}


function bouncer($q, $state, $stateParams) {
    const {kind, id} = $stateParams;
    const targetState = kindToViewState(kind);
    if (!targetState) {
        goToNotFound($state);
        return;
    }
    $state.go(targetState, {kind, id}, { location: false });
}


bouncer.$inject = [
    "$q",
    "$state",
    "$stateParams"
];


const baseState = {
    url: "entity"
};


const notFoundState = {
    template: "<h4>Sorry, unknown page type</h4>"
};


const byRefState = {
    url: "/ref/{kind:string}/{id:int}",
    resolve: { bouncer }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.entity", baseState)
        .state("main.entity.ref", byRefState)
        // .state("main.entity.extId", byExtIdState)
        .state("main.entity.not-found", notFoundState)
}


setup.$inject = [
    "$stateProvider",
];


export default setup;