/**
 * An attribute directive which allows
 * for create in page scroll to anchors.
 * Usage:  &lt;div waltz-jump-to='some-id'>&lt;/div>
 * @param $location
 * @param $anchorScroll
 * @returns directive
 */
const directive = function($location, $anchorScroll) {
    return {
        restrict: 'A',
        link: (scope, elem, attrs) => {
            // NOTE:  if you change the name of the directive
            // then the attr name will also change
            const target = attrs.waltzJumpTo;
            elem.on('click', () => {
                $anchorScroll.yOffset = 60;
                $location.hash(target);
                $anchorScroll();
                scope.$apply();
            });
        }
    }
};

directive.$inject=['$location', '$anchorScroll'];

export default directive;