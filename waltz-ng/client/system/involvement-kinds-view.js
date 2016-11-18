import _ from "lodash";
import {initialiseData} from "../common";

const initialState = {
    involvementKinds: [],
    creatinginvolvementKind: false,
    newinvolvementKind: {}
};


function controller($q,
                    involvementKindService,
                    notification) {

    const vm = initialiseData(this, initialState);

    function update(id, change) {
        const updateCmd = Object.assign(change, { id });
        return involvementKindService.update(updateCmd)
            .then(() => notification.success('Updated'));
    }

    vm.updateName = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { name: change })
            .then(() => _.find(vm.involvementKinds, {'id': id}).name = change.newVal);
    };

    vm.updateDescription = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { description: change })
            .then(() => _.find(vm.involvementKinds, {'id': id}).description = change.newVal);
    };


    vm.startNewinvolvementKind = () => {
        vm.creatinginvolvementKind = true;
    };

    vm.saveNewinvolvementKind = () => {
        involvementKindService
            .create(vm.newinvolvementKind)
            .then(id => {
                notification.success('Created');
                vm.creatinginvolvementKind = false;
                vm.newinvolvementKind = {};
                loadInvolvementKinds();
            });


    };

    vm.cancelNewinvolvementKind = () => {
        vm.creatinginvolvementKind = false;
        console.log('cancelled new');
    };


    function loadInvolvementKinds() {
        involvementKindService
            .loadInvolvementKinds()
            .then(kinds => {
                vm.involvementKinds = kinds;
            });
    };

    loadInvolvementKinds();
}


controller.$inject = [
    '$q',
    'InvolvementKindService',
    'Notification'
];


export default {
    template: require('./involvement-kinds-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
