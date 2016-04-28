
/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


const BINDINGS = {
    capabilities: '=',
    selectedNode: '='
};


function modalController($uibModalInstance, capabilities, initialSelection = {}) {
    const vm = this;

    vm.capabilities = capabilities;
    vm.selection = initialSelection;

    vm.ok = function () {
        $uibModalInstance.close(vm.selection);
    };

    vm.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}

modalController.$inject = [ '$uibModalInstance', 'capabilities', 'selection' ];



function controller($uibModal) {

    const vm = this;

    vm.animationsEnabled = true;

    vm.open = function (size) {

        var modalInstance = $uibModal.open({
            animation: vm.animationsEnabled,
            templateUrl: 'capabilities/popup-capability-selector-modal.html',
            controller: modalController,
            controllerAs: 'modal',
            size: size,
            resolve: {
                capabilities: function () {
                    return vm.capabilities;
                },
                selection: function() {
                    return vm.selectedNode
                }
            }
        });

        modalInstance.result.then(function (selection) {
            vm.selectedNode = selection;
        }, function () {
            //console.info('mc Modal dismissed at: ' + new Date());
        });
    };

    vm.toggleAnimation = function () {
        vm.animationsEnabled = !vm.animationsEnabled;
    };

}

controller.$inject = ['$uibModal'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./capability-selector-modal.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
