import _ from "lodash";

const NOTHING = {
    serverStats: [],
    databaseStats: [],
    softwareStats: []
};


function service($q,
                 serverInfoStore,
                 databaseStore,
                 softwareCatalogStore) {

    const findByAppIds = (appIds = [], id, kind, scope = 'CHILDREN') => {
        if (appIds.length == 0) { return $q.when(NOTHING); }

        if (_.some(appIds, _.isObject)) {
            // we've been given a list of objects, lets assume they are applications
            appIds = _.map(appIds, "id");
        }

        const promises = [
            serverInfoStore.findStatsForSelector(id, kind, scope),
            databaseStore.findStatsForAppIds(appIds),
            softwareCatalogStore.findStatsForAppIds(appIds)
        ];

        return $q
            .all(promises)
            .then(([
                serverStats,
                databaseStats,
                softwareStats
            ]) => ({
                    serverStats,
                    databaseStats,
                    softwareStats
                })
            );
    };



    return {
        findByAppIds
    }
}

service.$inject = [
    '$q',
    'ServerInfoStore',
    'DatabaseStore',
    'SoftwareCatalogStore'
];


export default service;