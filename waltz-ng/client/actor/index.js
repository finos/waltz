export default (module) => {

    module
        .service('ActorStore', require('./services/actor-store'))
        .service('ActorService', require('./services/actor-service'));
};
