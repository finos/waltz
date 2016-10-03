/**
 * Additional scroll amount to compensate for navbar etc.
*/
const OFFSET = 160;


/**
 * An attribute directive which allows
 * for create in page scroll to anchors.
 * Usage:  &lt;div waltz-jump-to='some-id'>&lt;/div>
 * @param $location
 * @param $anchorScroll
 * @returns directive
 */
const directive = function($anchorScroll,
                           $location) {
    return {
        restrict: 'A',
        link: (scope, elem, attrs) => {
            // NOTE:  if you change the name of the directive
            // then the attr name will also change
            const target = attrs.waltzJumpTo;
            elem.on('click', () => {
                $anchorScroll.yOffset = OFFSET;
                $location.hash(target);
                $anchorScroll();
                scope.$apply();
            });
        }
    }
};


directive.$inject=[
    '$anchorScroll',
    '$location'
];


export default directive;