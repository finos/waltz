/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {CORE_API} from '../../common/services/core-api-utils';


const flowDiagramsWidget = {
    template: `
        <waltz-flow-diagrams-section parent-entity-ref="$ctrl.parentEntityRef"
                                     can-create="true"
                                     create-diagram-commands="$ctrl.createDiagramCommands">
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
        id: 25821
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
        costsWidget,
        changesWidget]
};


function controller()
{
    const vm = Object.assign(this, initData);

    vm.$onInit = () => {
        vm.addWidget(bookmarkWidget)
    };

    vm.addWidget = w => {
        vm.widgets =  _.reject(vm.widgets, x => x.id === w.id)
        vm.widgets.unshift(w);
    };

    vm.additionalScope = {
        createDiagramCommands: () => {
            const app = vm.parentEntityRef;
            const title = `${app.name} flows`;
            const annotation = {
                id: +new Date()+'',
                kind: 'ANNOTATION',
                entityReference: app,
                note: `${app.name} data flows`
            };

            const modelCommands = [
                { command: 'ADD_NODE', payload: app },
                { command: 'ADD_ANNOTATION', payload: annotation },
                { command: 'SET_TITLE', payload: title }
            ];

            const moveCommands = [
                { command: 'MOVE', payload: { id: `ANNOTATION/${annotation.id}`, dx: 100, dy: -50 }},
                { command: 'MOVE', payload: { id: `APPLICATION/${app.id}`, dx: 300, dy: 200 }},
            ];

            return _.concat(modelCommands, moveCommands);
        }
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