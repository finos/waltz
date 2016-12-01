import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
};


const initialState = {
    query: '',
    searchResults: {
        show: false,
        apps: [],
        people: [],
        capabilities: [],
        orgUnits: []
    }
};


const template = require('./navbar-search-form.html');


function controller($timeout,
                    actorStore,
                    applicationStore,
                    capabilityStore,
                    personStore,
                    physicalFlowStore,
                    orgUnitStore) {
    const searchResults = {
        show: false
    };

    const vm = initialiseData(this, initialState);

    // helper fn, to reduce boilerplate
    const handleSearch = (query, store, resultKey) => {
        return store
            .search(query)
            .then(r => searchResults[resultKey] = r || []);
    };

    const doSearch = (query) => {
        if (_.isEmpty(query)) {
            searchResults.show = false;
        } else {
            searchResults.show = true;
            handleSearch(query, applicationStore, 'apps');
            handleSearch(query, personStore, 'people');
            handleSearch(query, capabilityStore, 'capabilities');
            handleSearch(query, orgUnitStore, 'orgUnits');
            handleSearch(query, actorStore, 'actors');

            physicalFlowStore
                .searchReports(query)
                .then(xs => searchResults.reports = xs || []);
        }
    };

    const dismissResults = (e) => $timeout(() => { searchResults.show = false; }, 200);

    vm.searchResults = searchResults;
    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => searchResults.show;
    vm.dismissResults = dismissResults;
}


controller.$inject = [
    '$timeout',
    'ActorStore',
    'ApplicationStore',
    'CapabilityStore',
    'PersonStore',
    'PhysicalFlowStore',
    'OrgUnitStore'
];


const component = {
    bindings,
    controller,
    template
};


export default component;