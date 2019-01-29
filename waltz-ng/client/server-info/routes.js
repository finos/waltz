import { CORE_API } from "../common/services/core-api-utils";
import ServerView from "./pages/view/server-view";


const baseState = {
    url: "server",
};


const viewState = {
    url: "/{id:int}",
    views: {
        "content@": ServerView.id
    }
};


const serverViewByExternalIdBouncerState = {
    url: "/external-id/{externalId}",
    resolve: { externalIdBouncer }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.server", baseState)
        .state("main.server.view", viewState)
        .state("main.server.external-id", serverViewByExternalIdBouncerState);
}

setup.$inject = [
    "$stateProvider"
];


export default setup;


function externalIdBouncer($state, $stateParams, serviceBroker) {
    const externalId = $stateParams.externalId;
    serviceBroker
        .loadViewData(CORE_API.ServerInfoStore.getByExternalId, [externalId])
        .then(r => {
            const element = r.data;
            if(element) {
                $state.go("main.server.view", {id: element.id});
            } else {
                console.log(`Cannot find server corresponding to external id: ${externalId}`);
            }
        });
}

externalIdBouncer.$inject = [
    "$state",
    "$stateParams",
    "ServiceBroker"
];