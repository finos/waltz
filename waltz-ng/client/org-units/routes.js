import ListView from "./list-view";
import UnitView from "./unit-view";
import {orgUnitsResolver, appTalliesResolver, endUserAppTalliesResolver} from "./resolvers.js";


const baseState = {
    resolve: {
        appTallies: appTalliesResolver,
        endUserAppTallies: endUserAppTalliesResolver,
        orgUnits: orgUnitsResolver
    }
};

const listState = {
    url: 'org-units',
    views: {'content@': ListView}
};


const viewState = {
    url: 'org-units/{id:int}',
    views: {
        'content@': UnitView
    }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.org-unit', baseState)
        .state('main.org-unit.list', listState)
        .state('main.org-unit.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;