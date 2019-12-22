/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import template from './static-panels-view.html';
import {initialiseData} from '../common';


const titleField = {
    type: 'input',
    key: 'title',
    templateOptions: {
        label: 'Title',
        description: 'Only shown if panel rendered in a section',
        required: true
    }
};

const groupField = {
    type: 'input',
    key: 'group',
    templateOptions: {
        label: 'Group',
        description: 'Group determines on what screens this panel will appear',
        required: true
    }
};

const iconField = {
    type: 'input',
    key: 'icon',
    templateOptions: {
        label: 'Icon',
        placeholder: 'Group determines on what screens this panel will appear',
        description: "Icon names are base on fontawesome names (without the 'fa-' prefix)",
        required: true
    }
};


const priorityField = {
    type: 'number',
    key: 'priority',
    templateOptions: {
        label: 'Priority',
        description: "If multiple panels belong to the same group, priority will be used to order them (ascending)",
        required: true
    }
};


const widthField = {
    type: 'number',
    key: 'width',
    templateOptions: {
        label: 'Width',
        description: "If rendered in a grid determines the number of cells to span (1-12)",
        required: true
    }
};


const contentField = {
    type: 'html',
    key: 'content',
    templateOptions: {
        label: 'Content',
        description: "HTML code, any paths should be absolute",
        required: true
    }
};



const fieldLayout = [
    {
        className: 'row',
        fieldGroup: [
            {className: 'col-xs-8', fieldGroup: [ titleField ]},
            {className: 'col-xs-4', fieldGroup: [ iconField ]}
        ]
    }, {
        className: 'row',
        fieldGroup: [
            {className: 'col-xs-8', fieldGroup: [ groupField ]},
            {className: 'col-xs-4', fieldGroup: [ priorityField ]}
        ]
    }, {
        className: 'row',
        fieldGroup: [
            {className: 'col-xs-8', fieldGroup: [ contentField ]},
            {className: 'col-xs-4', fieldGroup: [ widthField ]}
        ]
    }
];


const initialState = {
    fieldLayout
};


function controller(staticPanelStore) {

    const vm = initialiseData(this, initialState);

    const loadPanels = () =>
        staticPanelStore
            .findAll()
            .then(ps => vm.staticPanels = ps);

    // -- LOAD ---

    loadPanels();

    // -- INTERACT ---

    vm.select = (panel) => {
        vm.selected = panel;
        vm.formModel = Object.assign({}, panel);

    };

    vm.dismiss = () => {
        vm.selected = null;
        vm.formModel = null;
    };

    vm.onAddClicked = () => {
        const base = {
            icon: 'info',
            content:
`<div>
    <h2>My content</h2>
</div>`
        };

        vm.selected = base;
        vm.formModel = Object.assign({}, base);
    };

    vm.onSubmit = () => {
        staticPanelStore
            .save(vm.formModel)
            .then(() => {
                loadPanels();
                vm.dismiss();
            });
    };

}


controller.$inject = [
    'StaticPanelStore'
];


const page = {
    controller,
    template,
    controllerAs: 'ctrl'
};


export default page;