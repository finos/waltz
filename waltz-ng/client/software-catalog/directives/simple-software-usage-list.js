import _ from "lodash";


const BINDINGS = {
    catalog: '='
};

const defaultCatalog = {
    usages: [],
    packages: []
};

function controller($scope) {

    const vm = this;

    $scope.$watch(
        'ctrl.catalog',
        (catalog = defaultCatalog) => {
            const usagesByPkgId = _.countBy(catalog.usages, usage => usage.softwarePackageId);
            vm.countUsages = (pkg) => {
                const count = usagesByPkgId[pkg.id]
                return count > 1
                        ? 'x ' + count
                        : '';
            };

        }
    );



}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./simple-software-usage-list.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
