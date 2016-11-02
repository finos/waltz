export default (module) => {

    module
        .service('ActorStore', require('./services/actor-store'))
        .service('ActorService', require('./services/actor-service'));

    module
        .component('waltzActorSelector', require('./components/actor-selector'))
        .component('waltzBasicActorSelector', require('./components/basic-actor-selector'));

};
