
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

//import papa from 'papaparse';

function directive() {
    return {
        restrict: 'E',
        replace: true,
        template: '<input class="" type="file">',
        scope: {
            onSelect: '='
        },
        controllerAs: 'ctrl',
        controller: () => {

        },
        link: (scope, elem, attrs) => {
            elem.on('change', (evt) => {
                const { files } = evt.target;
                scope.onSelect(files);
            });

            if (attrs.multiple !== null) {
                elem.attr('multiple', 'multiple');
            }

        }
    };
}

function controller($http, baseUrl) {

    let reader;
    const vm = this;

    function errorHandler(evt) {
        switch (evt.target.error.code) {
            case evt.target.error.NOT_FOUND_ERR:
                alert('File Not Found!');
                break;
            case evt.target.error.NOT_READABLE_ERR:
                alert('File is not readable');
                break;
            case evt.target.error.ABORT_ERR:
                break; // noop
            default:
                alert('An error occurred reading this file.');
        }
    }

    function updateProgress(evt) {
        console.log('Progress', evt.target.result.length);


    }

    function read(file) {
        reader = new FileReader();
        reader.onload = (evt) => {
            const rawData = evt.target.result;

            const lines = _.map(
                rawData.replace('\n\r', '\n').split('\n'),
                (line, idx) => ({ idx, line}));

            vm.lines = lines;
            $http.post(`${baseUrl}/file-utils`, lines);
        }
        reader.onerror = errorHandler;


        const blob = file.slice(0, 1024 * 1024);
        reader.readAsBinaryString(blob);

    }

    vm.fileSelected = function(files) {
        console.log('oC', files, files[0]);
        read(files[0]);
    };

}


controller.$inject = [
    '$http',
    'BaseApiUrl'
];


const playpenView = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default (module) => {

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.playpen', {
                    url: 'playpen',
                    views: {'content@': playpenView }
                });
        }
    ]);

    module.directive('waltzFileSelect', directive);

};
