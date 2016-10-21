import _ from "lodash";
import {initialiseData} from "../common";

const initialState = {
    actors: [],
    creatingActor: false,
    newActor: {}
};


function controller($q,
                    actorService,
                    notification) {

    const vm = initialiseData(this, initialState);

    function update(id, change) {
        const updateCmd = Object.assign(change, { id });
        return actorService.update(updateCmd)
            .then(() => notification.success('Updated'));
    }

    vm.updateName = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { name: change })
            .then(() => _.find(vm.actors, {'id': id}).name = change.newVal);
    };

    vm.updateDescription = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { description: change })
            .then(() => _.find(vm.actors, {'id': id}).description = change.newVal);
    };


    vm.startNewActor = () => {
        vm.creatingActor = true;
    };

    vm.saveNewActor = () => {
        actorService
            .create(vm.newActor)
            .then(id => {
                notification.success('Created');
                vm.creatingActor = false;
                vm.newActor = {};
                loadActors();
            });


    };

    vm.cancelNewActor = () => {
        vm.creatingActor = false;
        console.log('cancelled new');
    };


    function loadActors() {
        actorService
            .loadActors()
            .then(kinds => {
                vm.actors = kinds;
            });
    };

    loadActors();
}


controller.$inject = [
    '$q',
    'ActorService',
    'Notification'
];


export default {
    template: require('./actors-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
