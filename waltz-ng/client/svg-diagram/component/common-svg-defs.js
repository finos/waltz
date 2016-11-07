import d3 from 'd3';

const bindings = {};


const template = require('./common-svg-defs.html');


function controller($element) {

    const defs = d3
        .select($element[0])
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
        .attr({
            id: d => d.name,
            refX: 20,
            refY: 4,
            markerWidth: 8,
            markerHeight: 8,
            orient: 'auto',
            stroke: d => d3.rgb(d.color).hsl().darker(0.5),
            fill: d => d3.rgb(d.color).hsl().brighter(1.5),
        })
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