
export default (module) => {

    module.config(require('./routes'));

    module
        .directive(
            'waltzProcessList',
             require('./directives/process-list'))
        .directive(
            'waltzProcessSection',
             require('./directives/process-section'));

    module.service(
        'ProcessStore',
        require('./services/process-store'));
}
