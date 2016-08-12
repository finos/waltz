function service($injector) {

    const resolve = (params, resolverName) => {
        if(!resolverName) return params;
        const specificResolver = $injector.get(resolverName);
        return specificResolver.resolve(params);
    };

    return {
        resolve
    };
}


service.$inject = [
    '$injector'
];


export default service;