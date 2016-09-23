import _ from "lodash";
import namedSettings from "./system/named-settings";

function run($http, settingsService) {

    settingsService
        .isDevModeEnabled()
        .then(devModeEnabled => {
            if (devModeEnabled) {

                settingsService.findAll()
                    .then(settings => {

                        console.log('Dev Extensions enabled');
                        _.chain(settings)
                            .filter(s => s.name.startsWith(namedSettings.httpHeaderPrefix))
                            .each(s => {
                                const headerName = s.name.replace(namedSettings.httpHeaderPrefix, '');
                                $http.defaults.headers.common[headerName] = s.value;
                            })
                            .value()

                    })

            }
        });

}

run.$inject = [
    '$http',
    'SettingsService'
];


function configure($httpProvider) {

    $httpProvider.defaults.cache = false;

    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }

    // disable IE ajax request caching
    $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';

    // using apply async should improve performance
    $httpProvider.useApplyAsync(true);
}

configure.$inject = [
    '$httpProvider'
];


function setupNetworking(module) {
    const baseUrl =
        __ENV__ === 'prod'
            ? './'
            : __ENV__ === 'test'
            ? 'http://192.168.1.179:8443/'
            : 'http://localhost:8443/';
    //: 'http://192.168.1.147:8443/'  // TODO (if testing IE on Mac) : use ip lookup

    module
        .constant('BaseApiUrl', baseUrl + 'api')
        .constant('BaseUrl', baseUrl)
        .constant('BaseExtractUrl', baseUrl + 'data-extract')
        .config(configure)
        .run(run);
}



export default setupNetworking;