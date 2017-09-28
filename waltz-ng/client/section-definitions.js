/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2017  Khartec Ltd.
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

export const flowDiagramsWidget = {
    template: `
        <waltz-flow-diagrams-section parent-entity-ref="$ctrl.parentEntityRef"
                                     can-create="true">
        </waltz-flow-diagrams-section>`,
    id: 'flow-diagrams-widget',
    name: 'Flow Diagrams',
    icon: 'picture-o'
};

export const changeInitiativesWidget = {
    template: `
        <waltz-change-initiative-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-change-initiative-section>`,
    id: 'change-initiatives-section',
    name: 'Change Initiatives',
    icon: 'paper-plane-o'
};

export const costsWidget = {
    template: `
        <waltz-app-costs-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-app-costs-section>`,
    id: 'app-costs-widget',
    name: 'Costs',
    icon: 'money'
};

export const peopleWidget = {
    template: `
        <waltz-involved-people-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-involved-people-section>`,
    id: 'people-widget',
    name: 'People',
    icon: 'users'
};

export const technologyWidget = {
    template: `
        <waltz-technology-section parent-entity-ref="$ctrl.parentEntityRef" >
        </waltz-technology-section>`,
    id: 'technology-widget',
    name: 'Technology',
    icon: 'server'
};

export const entityNamedNoteWidget = {
    template: `
        <waltz-entity-named-notes-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-entity-named-notes-section>`,
    id: 'entity-named-notes-widget',
    name: 'Notes',
    icon: 'sticky-note-o'
};

export const changesWidget = {
    template: `
        <waltz-change-log-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-change-log-section>`,
    id: 'changes-widget',
    icon: 'history',
    name: 'Changes'
};

export const flowWidget = {
    template: `
        <waltz-data-flow-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-data-flow-section>`,
    id: 'data-flow-widget',
    name: 'Data Flows',
    icon: 'random'
};

export const bookmarkWidget = {
    template: `
        <waltz-bookmarks-section parent-entity-ref="$ctrl.parentEntityRef"
                                 show-filter="true">
        </waltz-bookmarks-section>`,
    id: 'bookmark-widget',
    name: 'Bookmarks',
    icon: 'rocket'
};

export const measurableRatingsWidget = {
    template: `
        <waltz-measurable-rating-app-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-measurable-rating-app-section>`,
    id: 'ratings-section',
    name: 'Ratings',
    icon: 'puzzle-piece'
};

export const indicatorsWidget = {
    template: `
        <waltz-entity-statistic-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-entity-statistic-section>`,
    id: 'entity-statistic-section',
    name: 'Indicators',
    icon: 'pie-chart'
};

export const surveysWidget = {
    template: `
        <waltz-survey-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-survey-section>`,
    id: 'survey-instance-list-section',
    name: 'Surveys',
    icon: 'wpforms'
};