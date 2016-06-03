
export default (module) => {

    module.config(require('./routes'));

    module.directive(
        'waltzProcessList',
        require('./directives/process-list'));

    module.service(
        'ProcessStore',
        require('./services/process-store'));
}
