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

import _ from 'lodash';


const flowDiagramsWidget = {
    template: `
        <waltz-flow-diagrams-section parent-entity-ref="$ctrl.parentEntityRef"
                                     can-create="true">
        </waltz-flow-diagrams-section>`,
    id: 'flow-diagrams-widget',
    name: 'Flow Diagrams',
    icon: 'picture-o'
};


const costsWidget = {
    template: `
        <waltz-app-costs-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-app-costs-section> `,
    id: 'app-costs-widget',
    name: 'Costs',
    icon: 'money'
};


const peopleWidget = {
    template: `
        <waltz-involved-people-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-involved-people-section> `,
    id: 'people-widget',
    name: 'People',
    icon: 'users'
};

const technologyWidget = {
    template: `
        <waltz-technology-section parent-entity-ref="$ctrl.parentEntityRef" >
        </waltz-technology-section>`,
    id: 'technology-widget',
    name: 'Technology',
    icon: 'server'
};


const entityNamedNoteWidget = {
    template: `
        <waltz-entity-named-notes-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-entity-named-notes-section>`,
    id: 'entity-named-notes-widget',
    name: 'Notes',
    icon: 'sticky-note-o'
};


const changesWidget = {
    template: `
        <waltz-change-log-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-change-log-section>`,
    id: 'changes-widget',
    icon: 'history',
    name: 'Changes'
};

const flowWidget = {
    template: `
        <waltz-data-flow-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-data-flow-section>`,
    id: 'data-flow-widget',
    name: 'Data Flows',
    icon: 'random'
};

const bookmarkWidget = {
    template: `
        <waltz-bookmarks-section parent-entity-ref="$ctrl.parentEntityRef"
                                 show-filter="true">
        </waltz-bookmarks-section>`,
    id: 'bookmark-widget',
    name: 'Bookmarks',
    icon: 'rocket'
};


const initData = {
    id: 134,
    parentEntityRef: {
        kind: 'APPLICATION',
        id: 28083
    },
    visibility: {
        flows: false
    },
    widgets: [],
    availableWidgets: [
        flowWidget,
        flowDiagramsWidget,
        bookmarkWidget,
        entityNamedNoteWidget,
        technologyWidget,
        peopleWidget,
        costsWidget,
        changesWidget]
};


function controller()
{
    const vm = Object.assign(this, initData);

    vm.$onInit = () => {
        // vm.addWidget(bookmarkWidget);
        // vm.addWidget(technologyWidget);
    };

    vm.addWidget = w => {
        vm.widgets =  _.reject(vm.widgets, x => x.id === w.id)
        vm.widgets.unshift(w);
    };

}


controller.$inject = ['$compile', 'ServiceBroker'];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;