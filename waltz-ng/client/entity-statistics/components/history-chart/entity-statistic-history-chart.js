import d3 from 'd3';
import _ from 'lodash';
import {initialiseData} from '../../../common';

const bindings = {
    history: '<'
};


const template = require('./entity-statistic-history-chart.html')


const initialState = {

};



function prepareSections(svg) {
    return {
        svg
    };
}


function draw(data = {}) {

}

/**
 * Note: it is v. important the $element is an element with some width,
 * simply placing this in a element like a waltz-section will cause it
 * to render with 0x0....
 * @param $element
 * @param $window
 * @param dataTypeService
 */
function controller($element, $window) {

    const vm = initialiseData(this, initialState);
    const svg = d3.select($element.find('svg')[0]);

    const svgSections = prepareSections(svg);

    const debouncedRender = _.debounce(() => {

        const elemWidth = $element
            .parent()[0]
            .clientWidth;

        const data = {};

        draw(svgSections, elemWidth, data);

    }, 100);

    vm.$onChanges = (changes) => debouncedRender();

    angular
        .element($window)
        .on('resize', () => debouncedRender());
}


controller.$inject = [
    '$element',
    '$window'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
