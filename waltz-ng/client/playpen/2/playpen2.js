import {event, selectAll, select} from 'd3-selection'

const svg = `
     
`;


function calculateBoundingRect(clientRect, referenceElement) {
    const hPosX = referenceElement.getBoundingClientRect().left;
    const hPosY = referenceElement.getBoundingClientRect().top;

    return {
        x: clientRect.left - hPosX,
        y: clientRect.top - hPosY,
        width: clientRect.width,
        height: clientRect.height
    };
}


function controller(notification,
                    $element) {

    const vm = Object.assign(this, {});

    const highlight = (elem) => {

        const dimensions = calculateBoundingRect(
            elem.getBoundingClientRect(),
            document.getElementById('graphic'));

        select('#highlighter')
            .attr('x', dimensions.x)
            .attr('y', dimensions.y)
            .attr('width', dimensions.width)
            .attr('height', dimensions.height);
    };

    vm.annotate = () => {

        selectAll('g')
            .each(function(g) {
                const dimensions = calculateBoundingRect(
                    select(this)
                        .node()
                        .getBoundingClientRect(),
                    document
                        .getElementById('graphic'));

                select(this)
                    .insert('rect', ":first-child")
                    .attr('visibility', 'hidden')
                    .style('pointer-events', 'fill')
                    .attr('x', dimensions.x)
                    .attr('y', dimensions.y)
                    .attr('width', dimensions.width)
                    .attr('height', dimensions.height);
            });

        document.getElementById('graphic').onmousemove =
            e => {
                const mx = e.clientX;
                const my = e.clientY;
                const elementMouseIsOver = document.elementFromPoint(mx, my);
                highlight(elementMouseIsOver);
            };
    }


}



controller.$inject = [
    'Notification',
    '$element'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
