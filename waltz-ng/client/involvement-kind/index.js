export default (module) => {

    module
        .service('InvolvementKindStore', require('./services/involvement-kind-store'))
        .service('InvolvementKindService', require('./services/involvement-kind-service'));
};
