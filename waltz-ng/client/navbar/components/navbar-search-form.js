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
                    applicationStore,
                    capabilityStore,
                    personStore,
                    orgUnitStore) {
    const searchResults = {
        show: false
    };

    const vm = _.defaultsDeep(this, initialState);

    const doSearch = (query) => {
        if (_.isEmpty(query)) {
            searchResults.show = false;
        } else {
            searchResults.show = true;
            applicationStore
                .search(query)
                .then(r => searchResults.apps = r);
            personStore
                .search(query)
                .then(r => searchResults.people = r);
            capabilityStore
                .search(query)
                .then(r => searchResults.capabilities = r);
            orgUnitStore
                .search(query)
                .then(r => searchResults.orgUnits = r);
        }
    };

    const dismissResults = () => $timeout(() => { searchResults.show = false; }, 400);

    vm.searchResults = searchResults;
    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => searchResults.show;
    vm.dismissResults = dismissResults;
}

controller.$inject = [
    '$timeout',
    'ApplicationStore',
    'CapabilityStore',
    'PersonStore',
    'OrgUnitStore'
];


const component = {
    bindings,
    controller,
    template
};


export default component;