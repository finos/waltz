const NOTHING = {
    serverStats: [],
    databaseStats: [],
    softwareStats: []
};


function service($q,
                 serverInfoStore,
                 databaseStore,
                 softwareCatalogStore) {

    const findBySelector = (id, kind, scope = 'CHILDREN') => {

        const promises = [
            serverInfoStore.findStatsForSelector(id, kind, scope),
            //databaseStore.findStatsForSelector(id, kind, scope),
            softwareCatalogStore.findStatsForSelector(id, kind, scope)
        ];

        return $q
            .all(promises)
            .then(([
                serverStats,
              //  databaseStats,
                softwareStats
            ]) => ({
                    serverStats,
                //    databaseStats,
                    softwareStats
                })
            );
    };



    return {
        findBySelector
    }
}

service.$inject = [
    '$q',
    'ServerInfoStore',
    'DatabaseStore',
    'SoftwareCatalogStore'
];


export default service;