
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
import _ from 'lodash';


function controller() {
    const vm = this;

    const currentYear = new Date().getFullYear();
    const { start = { year: currentYear, quarter: 1 } } = vm;
    const { end = { year: start.year + 5, quarter: 4 } } = vm;

    vm.years = _.range(start.year, end.year + 1);
    vm.select = (yq) => {
        if (vm.onSelect()) {
            vm.onSelect()(yq);
        }
    };

    vm.isSelected = (y, q) => vm.selected && vm.selected.year === y && vm.selected.quarter === q;

    vm.isDisabled = (y, q) =>
            (y * 10 + q) < (start.year * 10 + start.quarter) ||
            (y * 10 + q) > (end.year * 10 + end.quarter);
}

export default function() {
    return {
        restrict: 'E',
        replace: true,
        template: require('./yq-select.html'),
        scope: {
            start: '=',
            end: '=',
            selected: '=',
            onSelect: '&'
        },
        bindToController: true,
        controllerAs: 'ctrl',
        controller
    };
}
