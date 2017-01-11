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
import {buildHierarchies, initialiseData, switchToParentIds} from '../common';
import {measurableKindNames} from '../common/services/display-names';
import {measurableKindDescriptions} from '../common/services/descriptions';
import {buildPropertySummer} from '../common/tally-utils';
import {scaleLinear} from 'd3-scale';


const initialState = {
    visibility: {
        tab: null
    },
    treeOptions: {
        nodeChildren: "children",
        dirSelectable: true,
        equality: function(node1, node2) {
            if (node1 && node2) {
                return node1.id === node2.id;
            } else {
                return false;
            }
        }
    }
};

const totalSummer = buildPropertySummer();


function prepareTabs(measurables = [], counts = []) {
    const countsById = _.keyBy(counts, 'id');

    const kinds = _.keys(measurableKindNames);
    const measurablesByKind = _.chain(measurables)
        .map(m => {
            const directCount = (countsById[m.id] || { count: 0 }).count;
            return Object.assign({}, m, { directCount })
        })
        .groupBy('kind')
        .value();

    const tabs = _.map(kinds, k => {
        const kind = {
            code: k,
            name: measurableKindNames[k],
            description: measurableKindDescriptions[k]
        };
        const measurablesForKind = measurablesByKind[k];
        const treeData = switchToParentIds(buildHierarchies(measurablesForKind));
        _.each(treeData, root => totalSummer(root));
        const maxCount = _.get(
            _.maxBy(treeData, 'totalCount'),
            'totalCount') || 0;

        const xScale = scaleLinear().range([0, 100]).domain([0, maxCount]);

        const expandedNodes = treeData.length < 6  // pre-expand small trees
            ? _.clone(treeData)
            : [];

        return {
            kind,
            measurables: measurablesForKind,
            treeData,
            expandedNodes,
            xScale
        };
    });

    return _.sortBy(
        tabs,
        g => g.kind.name);
}


function findFirstNonEmptyTab(tabs = []) {
    const tab = _.find(tabs, t => (t.measurables || []).length > 0);
    return (tab || tabs[0]).kind.code;
}


function controller($q,
                    $state,
                    $stateParams,
                    measurableStore,
                    measurableRatingStore,
                    staticPanelStore,
                    svgStore) {

    const vm = initialiseData(this, initialState);

    const measurablePromise = measurableStore
        .findAll();

    const countPromise = measurableRatingStore
        .countByMeasurable();

    $q.all([measurablePromise, countPromise])
        .then(([measurables = [], counts = []]) => {
            vm.tabs = prepareTabs(measurables, counts);
            vm.measurablesByExternalId = _.keyBy(measurables, 'externalId');
            vm.visibility.tab = $stateParams.kind || findFirstNonEmptyTab(vm.tabs);
        });

    staticPanelStore
        .findByGroups(_.chain(measurableKindNames)
            .keys()
            .map(kind => `HOME.${kind}`)
            .value())
        .then(panels => vm.panelsByKind = _.groupBy(
            panels,
            panel => panel.group.replace("HOME.", "")));

    svgStore
        .findByGroups(_.keys(measurableKindNames))
        .then(diagrams => vm.diagramsByKind = _.groupBy(diagrams, 'group'));

    vm.blockProcessor = b => {
        const extId = b.value;
        const measurable = vm.measurablesByExternalId[extId];
        if (measurable) {
            b.block.onclick = () => $state.go('main.measurable.view', { id: measurable.id });
            angular.element(b.block).addClass('clickable');
        } else {
            console.log(`MeasurableList: Could not find measurable with external id: ${extId}`, b)
        }
    };

}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'MeasurableStore',
    'MeasurableRatingStore',
    'StaticPanelStore',
    'SvgDiagramStore'
];


export default {
    template: require('./measurable-list.html'),
    controller,
    controllerAs: 'ctrl'
};
