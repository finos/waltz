function setup(module) {
    const registrationFns = [
        require('./routes'),
        require('./networking'),

        require('./common/componets'),
        require('./common/filters'),
        require('./common/services'),

        require('./access-log'),
        require('./actor'),
        require('./alias'),
        require('./applications'),
        require('./app-capabilities'),
        require('./app-groups'),
        require('./asset-cost'),
        require('./auth-sources'),
        require('./bookmarks'),
        require('./capabilities'),
        require('./change-initiative'),
        require('./complexity'),
        require('./change-log'),
        require('./databases'),
        require('./data-types'),
        require('./data-type-usage'),
        require('./end-user-apps'),
        require('./entity'),
        require('./entity-statistics'),
        require('./examples'),
        require('./formly'),
        require('./history'),
        require('./involvement'),
        require('./logical-flow'),
        require('./logical-flow-decorator'),
        require('./navbar'),
        require('./org-units'),
        require('./orphan'),
        require('./performance-metrics'),
        require('./perspectives'),
        require('./person'),
        require('./physical-flow-lineage'),
        require('./physical-flows'),
        require('./physical-specifications'),
        require('./playpen'),
        require('./process'),
        require('./profile'),
        require('./ratings'),
        require('./server-info'),
        require('./software-catalog'),
        require('./source-data-rating'),
        require('./static-panel'),
        require('./svg-diagram'),
        require('./system'),
        require('./technology'),
        require('./tour'),
        require('./traits'),
        require('./user'),
        require('./user-contribution'),
        require('./welcome'),
        require('./widgets'),
        require('./reports')
    ];


    _.each(registrationFns, (registrationFn, idx) => {
        if (!_.isFunction(registrationFn)) {
            console.error('cannot register: ', registrationFn, 'at idx', idx);
        }
        registrationFn(module);
    });
}


export default (module) => setup(module);