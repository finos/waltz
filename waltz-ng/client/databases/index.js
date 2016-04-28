const store = require('./services/database-store');
const piesDirective = require('./directives/database-pies');


export default (module) => {
    module.service('DatabaseStore', store);
    module.directive('waltzDatabasePies', piesDirective);
};
