export default (module) => {
    module
        .service('DatabaseStore', require('./services/database-store'));

    module
        .component('waltzDatabasePies', require('./components/database-pies'));
};
