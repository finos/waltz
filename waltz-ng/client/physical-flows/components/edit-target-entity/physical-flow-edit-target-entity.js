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
        .then((actors) => {
            vm.allActors = _.filter(actors,
                (a) => !(vm.owningEntity.kind === 'ACTOR' && a.id === vm.owningEntity.id));
        });

    const sameApp = (app) => vm.owningEntity.kind !== 'ACTOR' && app.id === vm.owningEntity.id;


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
        invokeFunction(vm.onChange, actor);
    };

    vm.cancelActor = () => {
        invokeFunction(vm.onDismiss);
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

