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

    const sameApp = (app) => vm.owningEntity.kind !== 'ACTOR' && app.id === vm.owningEntity.id;
    const sameActor = (actor) => vm.owningEntity.kind === 'ACTOR' && actor.id === vm.owningEntity.id;


    vm.$onChanges = (changes) => {
        if(vm.current && vm.current.kind) {
            console.log('kind: ', vm.current.kind);
            vm.entityKind = vm.current.kind === 'ACTOR' ? vm.current.kind : 'APPLICATION';
        }
    }


    vm.addApp = (app) => {
        vm.appDuplicate = sameApp(app)
        if(!vm.appDuplicate) {
            invokeFunction(vm.onChange, app);
        }
    };

    vm.cancelApp = () => {
        vm.appDuplicate = false;
        invokeFunction(vm.onDismiss);
    };

    vm.selectApp = (app) => {
        vm.appDuplicate = sameApp(app);
    };

    vm.addActor = (actor) => {
        vm.actorDuplicate = sameActor(actor);
        if(!vm.actorDuplicate) {
            invokeFunction(vm.onChange, actor);
        }
    };

    vm.cancelActor = () => {
        vm.actorDuplicate = false;
        invokeFunction(vm.onDismiss);
    };

    vm.selectActor = (actor) => {
        vm.actorDuplicate = sameActor(actor);
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

