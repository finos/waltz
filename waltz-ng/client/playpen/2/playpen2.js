import {termSearch} from "../../common";

const initData = {

};


function controller() {

    const vm = Object.assign(this, initData);

    vm.gridData = [{
        "firstName": "Cox",
            "lastName": "Carney",
            "company": "Enormo",
            "employed": true
        }, {
        "firstName": "Lorraine",
            "lastName": "Wise",
            "company": "Comveyer",
            "employed": false
        }, {
        "firstName": "Nancy",
            "lastName": "Waters",
            "company": "Fuelton",
            "employed": false
        }];

    vm.columnDefs = [
        {field: 'firstName'},
        {field: 'lastName'},
        {field: 'company'},
        {field: 'employed'}
    ];

    vm.filteredGridData = vm.gridData;

    const searchFields = [
        'firstName',
        'lastName',
        'company',
        'employed'
    ];

    vm.onGridInitialise = (e) => {
        vm.gridExporter = e.exportFn;
    };

    vm.exportWaltzGrid = () => {
        vm.gridExporter('playpen-grid-export.csv');
    };

    vm.filterData = queryText => {
        vm.filteredGridData = termSearch(vm.gridData, queryText, searchFields);
    };
}


controller.$inject = [

];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;