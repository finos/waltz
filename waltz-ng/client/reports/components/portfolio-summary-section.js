import _ from "lodash";
import {variableScale} from "../../common/colors";


function mkChartData(config, portfolios, employees) {

    const empIds = config.employeeIds;

    const statsById = _.reduce(empIds, (byId, empId) => {
        byId[empId] = _.countBy(portfolios[empId], app => app.lifecyclePhase);
        return byId;
    }, {});


    const productionAppsByOwner = {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            size: 120
        },
        data: _.chain(empIds)
            .map(empId => ({ key: employees[empId].displayName, count: statsById[empId].PRODUCTION || 0 }))
            .value()
    };



    const developmentAppsByOwner = {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            size: 120
        },
        data: _.chain(empIds)
            .map(empId => ({ key: employees[empId].displayName, count: statsById[empId].DEVELOPMENT || 0 }))
            .value()
    };
    /*
     const developmentByOwner = {
         config: {
             colorProvider: (d) => variableScale(d.data.key),
             size: 80
         },
         data: _.chain(apps)
             .countBy('kind')
             .map((v, k) => ({ key: k, count: v }))
             .value()
     };

 */
    return {
        productionAppsByOwner,
        developmentAppsByOwner
    };
}


function controller($scope) {

    const vm = this;

    $scope.$watchGroup(
        ['ctrl.portfolios', 'ctrl.reportConfig', 'ctrl.employees'],
        (([portfolios, reportConfig, employees]) => {
            if (portfolios && reportConfig && employees) {
                vm.chartData = mkChartData(reportConfig, portfolios, employees);
            }
        })
    );
}

controller.$inject = ['$scope'];


export default function() {
    return {
        restrict: 'E',
        controllerAs: 'ctrl',
        controller,
        template: require('./portfolio-summary-section.html'),
        scope: {
            reportConfig: '=',
            employees: '=',
            portfolios: '='
        },
        bindToController: true
    };
}