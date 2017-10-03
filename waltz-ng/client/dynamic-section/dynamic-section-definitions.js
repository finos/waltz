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

export const dynamicSections = [
    {
        componentId: 'apps-section',
        name: 'Applications',
        icon: 'desktop',
        id: 1,
        priority: 10,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'assets-costs-section',
        name: 'Costs',
        icon: 'money',
        id: 2,
        priority: 20,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'app-costs-section',
        name: 'Costs',
        icon: 'money',
        id: 3,
        priority: 80,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'auth-sources-section',
        name: 'Authoritative Sources',
        icon: 'shield',
        id: 4,
        priority: 20,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'bookmarks-section',
        name: 'Bookmarks',
        icon: 'rocket',
        id: 5,
        priority: 20,
        applicableEntityKinds: ['APPLICATION', 'APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'change-initiative-section',
        name: 'Change Initiatives',
        icon: 'paper-plane-o',
        id: 6,
        priority: 20,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'change-log-section',
        icon: 'history',
        name: 'Changes',
        id: 7,
        priority: 1000,
        applicableEntityKinds: ['APPLICATION', 'APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'complexity-section',
        icon: 'sort-numeric-asc',
        name: 'Application Complexity',
        id: 8,
        priority: 20,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'data-flow-section',
        name: 'Data Flows',
        icon: 'random',
        id: 9,
        priority: 60,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'entity-named-notes-section',
        name: 'Notes',
        icon: 'sticky-note-o',
        id: 10,
        priority: 20,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'entity-statistic-section',
        name: 'Indicators',
        icon: 'pie-chart',
        id: 11,
        priority: 90,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'flow-diagrams-section',
        name: 'Flow Diagrams',
        icon: 'picture-o',
        id: 12,
        priority: 40,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'involved-people-section',
        name: 'People',
        icon: 'users',
        id: 13,
        priority: 30,
        applicableEntityKinds: ['APPLICATION', 'APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'logical-flows-tabgroup-section',
        name: 'Logical Data Flows',
        icon: 'random',
        id: 14,
        priority: 50,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'measurable-rating-app-section',
        name: 'Ratings',
        icon: 'puzzle-piece',
        id: 15,
        priority: 10,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'measurable-ratings-browser-section',
        name: 'Viewpoint Ratings',
        icon: 'star-half-o',
        id: 16,
        priority: 20,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'CHANGE_INITIATIVE']
    }, {
        componentId: 'survey-section',
        name: 'Surveys',
        icon: 'wpforms',
        id: 17,
        priority: 100,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'technology-section',
        name: 'Technology',
        icon: 'server',
        id: 18,
        priority: 70,
        applicableEntityKinds: ['APPLICATION']
    }, {
        componentId: 'technology-summary-section',
        name: 'Technologies',
        icon: 'server',
        id: 19,
        priority: 20,
        applicableEntityKinds: ['APP_GROUP', 'ORG_UNIT', 'MEASURABLE', 'CHANGE_INITIATIVE']
    }
];