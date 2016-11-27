import {select} from 'd3-selection';
import {rgb} from 'd3-color';

const bindings = {};


const template = require('./common-svg-defs.html');


function controller($element) {

    const defs = select($element[0])
        .select('svg')
        .append('defs');

    const markers = [
        { name: 'arrowhead' , color: '#666' },
        { name: 'arrowhead-PRIMARY' , color: 'green' },
        { name: 'arrowhead-SECONDARY' , color: 'orange' },
        { name: 'arrowhead-DISCOURAGED' , color: 'red' },
        { name: 'arrowhead-NO_OPINION' , color: '#333' }
    ];

    defs.selectAll('marker')
        .data(markers, m => m.name)
        .enter()
        .append('marker')
        .attr('id', d => d.name)
        .attr('refX', 20)
        .attr('refY', 4)
        .attr('markerUnits', "strokeWidth")
        .attr('markerWidth', 8)
        .attr('markerHeight', 8)
        .attr('orient', 'auto')
        .attr('stroke', d => rgb(d.color).darker(0.5).toString())
        .attr('fill', d => rgb(d.color).brighter(1.5).toString())
        .append('path')
        .attr('d', 'M 0,0 V 8 L8,4 Z'); // this is actual shape for arrowhead
}


controller.$inject= ['$element'];


const component = {
    template,
    controller,
    bindings
};

export default component;