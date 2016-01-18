
import { lifecyclePhaseColorScale, variableScale } from '../../common/colors';


function mkAppChartData(apps) {

    const byLifecyclePhase = {
        config: {
            colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
            size: 80
        },
        data: _.chain(apps)
            .countBy('lifecyclePhase')
            .map((v, k) => ({ key: k, count: v }))
            .value()
    };

    const byKind = {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            size: 80
        },
        data: _.chain(apps)
            .countBy('kind')
            .map((v, k) => ({ key: k, count: v }))
            .value()
    };

    return {
        byLifecyclePhase,
        byKind
    };
}


function controller($scope) {

    const vm = this;

    $scope.$watch(
        'ctrl.portfolio',
        (portfolio => {
            vm.chartData = mkAppChartData(portfolio);
        })
    );
}

controller.$inject = ['$scope'];


export default function portfolioSection() {
    return {
        restrict: 'E',
        controllerAs: 'ctrl',
        controller,
        template: require('./portfolio-section.html'),
        scope: {
            employee: '=',
            portfolio: '='
        },
        bindToController: true
    };
}
