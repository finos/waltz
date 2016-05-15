import _ from "lodash";


const BINDINGS = {
    flowData: '=',
    onLoadDetail: '='
};


function controller($scope) {

    const vm = this;

    $scope.$watch(
        'ctrl.flowData.flows',
        flows => {
            if (!flows || flows.length == 0) return;

            const grouped = _.groupBy(flows, f => ([f.source.id, f.target.id]));

            const condensed = _.values(_.mapValues(
                grouped,
                v => ({
                    source: v[0].source,
                    target: v[0].target,
                    groupMembership: {
                        source: _.includes(vm.flowData.appIds, v[0].source.id),
                        target: _.includes(vm.flowData.appIds, v[0].target.id)
                    },
                    categories: _.map(v, "dataType")
                })));

            const entities = _.chain(flows)
                .flatMap(f => ([f.source, f.target]))
                .uniqBy(a => a.id)
                .value();

            vm.condensedFlows = condensed;
            vm.entities = entities;
        });

    vm.loadDetail = () => {
        if (vm.onLoadDetail) vm.onLoadDetail();
        else console.log("No handler for detail provided ('on-load-detail')");
    };
}

controller.$inject = [ '$scope' ];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    template: require('./data-flows-tabgroup.html')
};



export default () => directive;
