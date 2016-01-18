import d3 from 'd3';
import _ from 'lodash';

import { lifecyclePhaseDisplayNames }
    from '../../common/services/display_names';

import { lifecyclePhaseColorScale, riskRatingColorScale }
    from '../../common/colors';

function controller(uiGridConstants, $scope) {
    const vm = this;

    vm.gridOptions = {
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        columnDefs: [
            { field: 'name' },
            { field: 'kind' },
            { field: 'riskRating' },
            {
                field: 'lifecyclePhase',
                cellTemplate: '<div class="ui-grid-cell-contents"> {{ COL_FIELD | toDisplayName:"lifecyclePhase" }} </div>',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: _.map(lifecyclePhaseDisplayNames, (label, value) => ({ label, value }))
                }
            },
            {
                field: 'description',
                cellTooltip: (row) => row.entity.description
            }
        ],
        data: vm.endUserApps
    };

    const recalc = (apps) => {
        vm.gridOptions.data = apps;

        const toArr = tally => _.map(tally, (v, k) => ({ key: k, count: v }));

        const countByLifecyclePhase = _.countBy(apps, 'lifecyclePhase');
        const countByKind = _.countBy(apps, 'kind');
        const countByRiskRating = _.countBy(apps, 'riskRating');


        vm.pies.byLifecyclePhase.data = toArr(countByLifecyclePhase);
        vm.pies.byKind.data = toArr(countByKind);
        vm.pies.byRiskRating.data = toArr(countByRiskRating);

    };


    $scope.$watch('ctrl.endUserApps', (apps) => recalc(apps));

    const kindColorScale = _.compose(d3.rgb, d3.scale.category20());

    vm.pies = {
        byLifecyclePhase: {
            data: [],
            config: {
                size: 100,
                colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
                labelProvider: (d) => d.data.key
            }
        },
        byKind: {
            data: [],
            config: {
                size: 100,
                colorProvider: (d) => kindColorScale(d.data.key),
                labelProvider: (d) => d.data.key
            }
        },
        byRiskRating: {
            data: [],
            config: {
                size: 100,
                colorProvider: (d) => riskRatingColorScale(d.data.key),
                labelProvider: (d) => d.data.key
            }
        }
    };
}

controller.$inject = ['uiGridConstants', '$scope'];


export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./end-user-app-table.html'),
    scope: {},
    controllerAs: 'ctrl',
    bindToController: {
        endUserApps: '='
    },
    controller
});
