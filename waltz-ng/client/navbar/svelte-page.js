import template from "./svelte-page.html";
import Sidebar from "./Sidebar.svelte";
import {sidebarExpanded, sidebarVisible} from "./sidebar-store";
import Toasts from "../notification/components/toaster/Toasts.svelte";
import ToastStore from "../svelte-stores/toast-store"
import Popover from "../common/svelte/popover/Popover.svelte";
import {isIE} from "../common/browser-utils";
import namedSettings from "../system/named-settings";
import {CORE_API} from "../common/services/core-api-utils";

function controller($q, $scope, $timeout, settingsService, $rootScope, $auth) {
    const vm = this;

    $scope.isAuthFailed = false;
    $scope.oauthProvider = "";

    vm.Sidebar = Sidebar;
    vm.Toasts = Toasts;
    vm.Popover = Popover;

    const unsubExpand = sidebarExpanded.subscribe((d) => {
        $scope.$applyAsync(() => vm.isExpanded = d);
        $timeout(() => {}, 100); // nudging angular so things like ui-grid can auto resize
    });

    const unsubVisible = sidebarVisible.subscribe((d) => {
        $scope.$applyAsync(() => vm.isVisible = d);
    });

    vm.$onDestroy = () => {
        unsubExpand();
        unsubVisible();
    };

    function getOauthSettings() {

        const allowSSOLoginPromise = settingsService
            .findOrDefault("web.authentication", "")
            .then(r => r);

        const oauthProviderNamePromise = settingsService
            .findOrDefault(namedSettings.oauthProviderName, null)
            .then(r => r);

        const disableAnonymousPromise = settingsService
            .findOrDefault(namedSettings.oauthDisableAnonymous, false)
            .then(r => r);

        return $q
            .all([allowSSOLoginPromise, oauthProviderNamePromise, disableAnonymousPromise])
            .then(([webAuthentication, provider, disableAnonymous]) => {
                vm.allowSSOLogin = (webAuthentication === "sso");
                vm.oauthProviderName = provider;
                vm.oauthDisableAnonymous = (disableAnonymous === "true");
            });
    }

    $scope.isAuthenticated = function() {
        return $auth.isAuthenticated();
    }

    vm.$onInit = () => {
        if (isIE()) {
            settingsService
                .findOrDefault(
                    "ui.banner.message",
                    "Waltz is optimised for use in modern browsers. For example Google Chrome, Firefox and Microsoft Edge")
                .then(m => ToastStore.info(m, {timeout: 10000}));
        }

        // Check if sso login is enabled and invoke configured login options
        getOauthSettings()
        .then(() => {
            // sso option is enabled and oauth provider name configured in Settings
            if (vm.allowSSOLogin) {
                if(vm.oauthProviderName) {
                    console.log("oauthProviderName is set - Implement thirdparty sso oauth")

                    // try to authenticate with OAuth Provider
                    if (!$auth.isAuthenticated()){
                        $auth.authenticate(vm.oauthProviderName)
                        .then(function(response){
                            // console.log("authentication through " + vm.oauthProviderName + " - Successful");
                            window.location.reload();
                        })
                        .catch(function(response){
                            console.log("authentication through " + vm.oauthProviderName + " - FAILED");
                            $scope.isAuthFailed = true;
                            return false;
                        });
                    }

                } else {
                    // sso authentication implemented externally
                    console.log("sso implemented externally")
                }
            }
        });
    }
}

controller.$inject = [
    "$q",
    "$scope",
    "$timeout",
    "SettingsService",
    "$rootScope",
    "$auth"
];

const component = {
    template,
    controller,
    bindings: {}
};

export default {
    id: "waltzSveltePage",
    component
}