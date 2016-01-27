/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

function bucket(complexityData) {
    console.log(complexityData);
    _.chain(complexityData)
        .tap(x => console.log('tap', x))
        .map(d => ({ score: Math.round(d.overallScore * 50) / 50, id: d.id }))
        .value()
}


function render(config) {

    const data = bucket(config.complexity);

    const svg = d3.select(config.elem).selectAll('svg')
        .data([data])
        .enter()
        .append('svg')
        .attr({
            width: 600,
            height: 300
        });

}

function controller($scope) {
    const watcher = ([elem, complexity]) => {
        if (!elem || !complexity) return;
        render({ elem, complexity });
    };

    $scope.$watchGroup(['elem', 'ctrl.complexity'], watcher);
}

controller.$inject = ['$scope'];


function link(scope, elem, attr) {
    console.log('link', arguments)
    scope.elem = elem[0];
}

export default () => ({
    restrict: 'E',
    replace: true,
    link,
    controller,
    controllerAs: 'ctrl',
    scope: {},
    template: '<div class="viz-holder">Remove me</div>',
    bindToController: {
        complexity: '='
    }
});