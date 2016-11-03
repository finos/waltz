export default (module) => {

    module
        .config(require('./routes'))
        .service('ActorStore', require('./services/actor-store'))
        .service('ActorService', require('./services/actor-service'));

    module
        .component('waltzActorOverview', require('./components/actor-overview'))
        .component('waltzActorSelector', require('./components/actor-selector'))
        .component('waltzBasicActorSelector', require('./components/basic-actor-selector'));

};
