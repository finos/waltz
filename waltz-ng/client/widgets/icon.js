
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

export default () => ({
    restrict: 'E',
    replace: true,
    template: '<span style="font-size: smaller"><i class="{{classNames}}"/></span>',
    scope: {
        name: '@',
        size: '@',
        flip: '@',
        rotate: '@',
        stack: '@',
        fixedWidth: '@',
        inverse: '@',
        spin: '@'
    },
    link: (scope) => {
        scope.classNames = [
            'fa',
            `fa-${scope.name}`,
            scope.flip ? `fa-flip-${scope.flip}` : '',
            scope.rotate ? `fa-rotate-${scope.rotate}` : '',
            scope.size ? `fa-${scope.size}` : '',
            scope.stack ? `fa-stack-${scope.stack}` : '',
            scope.fixedWidth ? 'fa-fw' : '',
            scope.inverse ? 'fa-inverse' : '',
            scope.spin ? 'fa-spin' : ''
        ].join(' ');
    }
});

