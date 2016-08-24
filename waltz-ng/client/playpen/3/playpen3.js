import _ from 'lodash';


const initialState = {
    ref: {
        id: 10,
        kind: 'ORG_UNIT'
    }
};


function controller($scope, assetCostStore, appStore) {
    const vm = Object.assign(this, initialState);

    vm.refresh = () => {
        let appsById = {};

        const selector = {
            entityReference: vm.ref,
            scope: 'CHILDREN'
        };

        const amountsPromise = assetCostStore
            .calculateCombinedAmountsForSelector(selector);

        amountsPromise
            .then(amounts => vm.amounts = amounts);

        appStore
            .findBySelector(selector)
            .then(apps => vm.apps = apps)
            .then(apps => appsById = _.keyBy(apps, 'id'));

        vm.less = () =>
            amountsPromise
                .then(amounts =>
                    vm.amounts = _.filter(
                        amounts,
                        () => _.random(0, 10) > 9.3));


        vm.more = () =>
            amountsPromise
                .then(amounts =>
                    vm.amounts = _.filter(
                        amounts,
                        () => _.random(0, 10) > 5));

        vm.all = () =>
            amountsPromise
                .then(amounts =>
                    vm.amounts = amounts);

        vm.low = () =>
            amountsPromise
                .then(amounts =>
                    vm.amounts = _.filter(amounts, x => x.v2 < 400000));

        vm.high = () =>
            amountsPromise
                .then(amounts =>
                    vm.amounts = _.filter(amounts, x => x.v2 > 1400000));

        vm.onHover = ({v1: appId, v2: amount}) => $scope.$applyAsync(() => {
            vm.hoveredApp = appsById[appId];
            vm.hoveredAmount = amount;
        });

        vm.onSelect = ({v1: appId, v2: amount}) => $scope.$applyAsync(() => {
            vm.selectedApp = appsById[appId];
            vm.selectedAmount = amount;
        });
    };

    vm.refresh();
}


controller.$inject = [
    '$scope',
    'AssetCostStore',
    'ApplicationStore'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;