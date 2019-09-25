import {kindToViewState} from "../common/link-utils";
import {loadByExtId, toEntityRef} from "../common/entity-utils";
import picker from "./pages/picker";


function goToNotFound($state) {
    return $state.go("main.entity.not-found", {}, {location: false});
}


function goToEntity($state, targetState, entity) {
    return $state.go(
        targetState,
        { id: entity.id },
        { location: true });
}


const byRefState = {
    url: "/{kind:string}/id/{id:int}",
    resolve: { bouncer: refBouncer }
};


function refBouncer($q, $state, $stateParams) {
    const {kind, id} = $stateParams;
    const targetState = kindToViewState(kind);
    if (!targetState) {
        goToNotFound($state);
        return;
    }
    goToEntity($state, targetState, { kind, id });
}


refBouncer.$inject = [
    "$q",
    "$state",
    "$stateParams"
];


const byExtIdState = {
    url: "/{kind:string}/external-id/{extId:.*}",
    resolve: { matches: extIdBouncer },
    views: {
        "content@": picker
    }
};



function extIdBouncer($q, $state, $stateParams, serviceBroker) {
    const {kind, id} = $stateParams;
    const targetState = kindToViewState(kind);

    if (!targetState) {
        goToNotFound($state);
        return;
    }

    return loadByExtId(serviceBroker, kind, $stateParams.extId)
        .then(r => {
            switch (_.size(r)) {
                case 0:
                    return goToNotFound($state);
                case 1:
                    return goToEntity($state, targetState, _.first(r));
                default:
                    // by default we assume multiple and return them.  The state will pass these to the `picker` page
                    return Promise.resolve(r);
            }
        })
        .catch(e => {
            console.log("Failed to load entity by external identifier:", e);
            goToNotFound($state);
        });
}


extIdBouncer.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


const baseState = {
    url: "entity"
};


const notFoundState = {
    views: {
        "content@": {
            template: `
                <waltz-section name="No matches">
                    <waltz-no-data>
                        <message>Sorry, nothing matches the given criteria</message>
                    </waltz-no-data>
                </waltz-section>`
        }
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.entity", baseState)
        .state("main.entity.ref", byRefState)
        .state("main.entity.ext-id", byExtIdState)
        .state("main.entity.not-found", notFoundState);
}


setup.$inject = [
    "$stateProvider"
];


export default setup;