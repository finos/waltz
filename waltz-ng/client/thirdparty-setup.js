import thunk from "redux-thunk";
import rootReducer from "./reports/reducers";


function setup(module) {

    module.config([
        'uiSelectConfig',
        (uiSelectConfig) => {
            uiSelectConfig.theme = 'bootstrap';
            uiSelectConfig.resetSearchInput = true;
            uiSelectConfig.appendToBody = true;
        }
    ]);

    module.config([
        '$authProvider',
        'BaseUrl',
        function($authProvider, BaseUrl) {
            $authProvider.baseUrl = BaseUrl;
            $authProvider.withCredentials = false;

            $authProvider.google({
                clientId: 'Google account'
            });

            $authProvider.github({
                clientId: 'GitHub Client ID'
            });

            $authProvider.linkedin({
                clientId: 'LinkedIn Client ID'
            });

        }
    ]);

    module.config([
        '$ngReduxProvider',
        ($ngReduxProvider) => {
            $ngReduxProvider.createStoreWith(rootReducer, [thunk], []);
        }
    ]);

}


export default (module) => setup(module);