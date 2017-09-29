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

export const appsSection = {
    template: `
        <waltz-apps-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-apps-section>`,
    id: 'apps-section',
    name: 'Applications',
    icon: 'desktop'
};


export const assetCostsSection = {
    template: `
        <waltz-asset-costs-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-asset-costs-section>`,
    id: 'costs-section',
    name: 'Costs',
    icon: 'money'
};


export const appCostsSection = {
    template: `
        <waltz-app-costs-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-app-costs-section>`,
    id: 'app-costs-widget',
    name: 'Costs',
    icon: 'money'
};


export const authSourcesSection = {
    template: `
        <waltz-auth-sources-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-auth-sources-section>`,
    id: 'authoritative-sources-section',
    name: 'Authoritative Sources',
    icon: 'shield'
};


export const bookmarksSection = {
    template: `
        <waltz-bookmarks-section parent-entity-ref="$ctrl.parentEntityRef"
                                 show-filter="true">
        </waltz-bookmarks-section>`,
    id: 'bookmark-widget',
    name: 'Bookmarks',
    icon: 'rocket'
};


export const changeInitiativesSection = {
    template: `
        <waltz-change-initiative-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-change-initiative-section>`,
    id: 'change-initiatives-section',
    name: 'Change Initiatives',
    icon: 'paper-plane-o'
};


export const changeLogSection = {
    template: `
        <waltz-change-log-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-change-log-section>`,
    id: 'changes-widget',
    icon: 'history',
    name: 'Changes'
};


export const complexitySection = {
    template: `
        <waltz-complexity-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-complexity-section>`,
    id: 'complexity-section',
    icon: 'sort-numeric-asc',
    name: 'Application Complexity'
};


export const dataFlowSection = {
    template: `
        <waltz-data-flow-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-data-flow-section>`,
    id: 'data-flow-widget',
    name: 'Data Flows',
    icon: 'random'
};


export const entityNamedNotesSection = {
    template: `
        <waltz-entity-named-notes-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-entity-named-notes-section>`,
    id: 'entity-named-notes-widget',
    name: 'Notes',
    icon: 'sticky-note-o'
};


export const entityStatisticSection = {
    template: `
        <waltz-entity-statistic-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-entity-statistic-section>`,
    id: 'entity-statistic-section',
    name: 'Indicators',
    icon: 'pie-chart'
};


export const flowDiagramsSection = {
    template: `
        <waltz-flow-diagrams-section parent-entity-ref="$ctrl.parentEntityRef"
                                     can-create="true">
        </waltz-flow-diagrams-section>`,
    id: 'flow-diagrams-widget',
    name: 'Flow Diagrams',
    icon: 'picture-o'
};


export const involvedPeopleSection = {
    template: `
        <waltz-involved-people-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-involved-people-section>`,
    id: 'people-widget',
    name: 'People',
    icon: 'users'
};


export const logicalFlowTabgroupSection = {
    template: `
        <waltz-logical-flows-tabgroup-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-logical-flows-tabgroup-section>`,
    id: 'logical-flows-section',
    name: 'Logical Data Flows',
    icon: 'random'
};


export const measurableRatingAppSection = {
    template: `
        <waltz-measurable-rating-app-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-measurable-rating-app-section>`,
    id: 'ratings-section',
    name: 'Ratings',
    icon: 'puzzle-piece'
};


export const measurableRatingsBrowserSection = {
    template: `
        <waltz-measurable-ratings-browser-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-measurable-ratings-browser-section>`,
    id: 'ratings-explorer-section',
    name: 'Viewpoint Ratings',
    icon: 'star-half-o'
};


export const surveySection = {
    template: `
        <waltz-survey-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-survey-section>`,
    id: 'survey-instance-list-section',
    name: 'Surveys',
    icon: 'wpforms'
};


export const technologySection = {
    template: `
        <waltz-technology-section parent-entity-ref="$ctrl.parentEntityRef" >
        </waltz-technology-section>`,
    id: 'technology-widget',
    name: 'Technology',
    icon: 'server'
};


export const technologySummarySection = {
    template: `
        <waltz-technology-summary-section parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-technology-summary-section>`,
    id: 'technologies-section',
    name: 'Technologies',
    icon: 'server'
};