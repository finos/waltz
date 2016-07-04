function setup(module) {
    const registrationFns = [
        require('./routes'),
        require('./networking'),

        require('./common/directives'),
        require('./common/filters'),
        require('./common/services'),

        require('./access-log'),
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
        require('./data-flow'),
        require('./data-types'),
        require('./end-user-apps'),
        require('./entity-statistics'),
        require('./examples'),
        require('./history'),
        require('./involvement'),
        require('./navbar'),
        require('./org-units'),
        require('./performance-metrics'),
        require('./perspectives'),
        require('./person'),
        require('./playpen'),
        require('./process'),
        require('./profile'),
        require('./ratings'),
        require('./server-info'),
        require('./settings'),
        require('./software-catalog'),
        require('./source-data-rating'),
        require('./static-panel'),
        require('./svg-diagram'),
        require('./technology'),
        require('./traits'),
        require('./user'),
        require('./user-contribution'),
        require('./formly'),
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