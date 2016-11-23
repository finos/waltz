import {initialiseData} from "../../common";

const bindings = {
    entries: '<',
    onInitialise: '<'
};

const template = require('./change-log-table.html');


const initialState = {
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        {
            field: 'severity',
            name: 'Severity',
            width: '10%',
            cellFilter: "toDisplayName:'severity'"
        },
        {
            field: 'message',
            name: 'Message',
            width: '70%'
        },
        {
            field: 'userId',
            name: 'User',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.profile.view ({userId: COL_FIELD})"><span ng-bind="COL_FIELD"></span></a></div>'
        },
        {
            field: 'createdAt',
            name: 'Timestamp',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        }
    ];

}


const component = {
    bindings,
    template,
    controller
};


export default component;
