/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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