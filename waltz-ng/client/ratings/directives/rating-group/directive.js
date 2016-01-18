import _ from 'lodash';
import angular from 'angular';

import { init, draw } from './diagram';


function link(scope, elem) {
    scope.vizElem = elem[0].querySelector('.viz');
    scope.svg = init(scope.vizElem);
}


function controller(scope, $window) {

    const debouncedRender = _.debounce(() => {
        if (!( scope.vizElem
            && scope.data
            && scope.tweakers)) return;

        const { tweakers } = scope;

        const highestRatingCount = scope.highestRatingCount || scope.data.raw.length;

        draw(
            scope.data,
            scope.vizElem.offsetWidth,
            scope.svg,
            tweakers,
            highestRatingCount);

    }, 100);


    angular.element($window).on('resize', () => debouncedRender());
    scope.$watch('maxGroupRatings', () => debouncedRender());
    scope.$watch('data', () => debouncedRender(), true);
    scope.$watch('highestRatingCount', () => debouncedRender());
}


controller.$inject = ['$scope', '$window'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        data: '=',
        tweakers: '=',
        highestRatingCount: '='
    },
    template: '<div><div class="viz"></div></div>',
    link,
    controller
});
