import {initialiseData, invokeFunction} from "../../../common";


const bindings = {
    current: '<',
    owningEntity: '<',
    onDismiss: '<',
    onChange: '<'
};


const template = require('./physical-flow-edit-target-entity.html');


const initialState = {
    entityKind: 'APPLICATION'
};


function controller(actorStore) {
    const vm = initialiseData(this, initialState);

    actorStore
        .findAll()
        .then((actors) => vm.allActors = actors );

    const sameApp = (app, owningEntity) => owningEntity.kind !== 'ACTOR' && app.id === owningEntity.id;
    const sameActor = (actor, owningEntity) => owningEntity.kind === 'ACTOR' && actor.id === owningEntity.id;


    vm.$onChanges = (changes) => {
        if(vm.current && vm.current.kind) {
            vm.entityKind = vm.current.kind === 'ACTOR' ? vm.current.kind : 'APPLICATION';
        }
    }


    vm.addApp = (app) => {
        vm.appDuplicate = sameApp(app, vm.owningEntity)
        if(!vm.appDuplicate) {
            invokeFunction(vm.onChange, app);
        }
    };

    vm.cancelApp = () => {
        vm.appDuplicate = false;
        invokeFunction(vm.onDismiss);
    };

    vm.selectApp = (app) => {
        vm.appDuplicate = sameApp(app, vm.owningEntity);
    };

    vm.addActor = (actor) => {
        vm.actorDuplicate = sameActor(actor, vm.owningEntity);
        if(!vm.actorDuplicate) {
            invokeFunction(vm.onChange, actor);
        }
    };

    vm.cancelActor = () => {
        vm.actorDuplicate = false;
        invokeFunction(vm.onDismiss);
    };

    vm.selectActor = (actor) => {
        vm.actorDuplicate = sameActor(actor, vm.owningEntity);
    };

}


controller.$inject = [
    'ActorStore'
];


const component = {
    bindings,
    template,
    controller
};


export default component;

