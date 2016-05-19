/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

const BINDINGS = {
    ratings: '='
};


function controller($scope) {

    const vm = this;

    vm.accuracyRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Trusted";
            case 'A':
                return "Indeterminate";
            case 'R':
                return "Untrusted";
            default:
                return "Unknown";
        }
    };

    vm.authoritativenessRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Golden";
            case 'A':
                return "Indirect";
            case 'R':
                return "'Found'";
            default:
                return "Unknown";
        }
    };

    vm.completenessRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Complete";
            case 'A':
                return "Partial";
            case 'R':
                return "Sparse";
            default:
                return "Unknown";
        }
    };

  

};

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./source-data-info.html')
});

