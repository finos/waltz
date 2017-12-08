import {initialiseData} from '../../../common';
import {kindToViewState} from "../../../common/link-utils";
import {stringToRef} from "../../../common/entity-utils";
import {select, selectAll, event} from 'd3-selection';
import {drag} from 'd3-drag';
import {zoom} from 'd3-zoom';

import template from './entity-svg-diagram-viewer.html';


const bindings = {
    svg: '<'
};


const initialState = {
    panZoomEnabled: false,
    popup: {
        style: {
            position: 'absolute',
            background: 'white',
            display: 'none'
        },
        text: ""
    }
};


function controller($element, $state, $timeout) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const holder = $element.find('div')[0];
        holder.innerHTML= vm.svg;

        let currentZoom = 1;
        let currentTranslate = [0,0];

        const zoomed = () => {
            const transform = event.transform;

            if (transform) {
                currentZoom = transform.k;
                currentTranslate[0] = transform.x;
                currentTranslate[1] = transform.y;
                updateTransform();
            }
        };

        const updateTransform = () =>
            g.attr('transform', `scale(${currentZoom}) translate(${currentTranslate})`);

        const svg = select(holder)
            .select("svg");

        const g = svg
            .select("g");

        const myZoom = zoom()
            .on("zoom", zoomed);

        const setupZoom = () => {
            svg.call(myZoom)
                .on('dblclick.zoom', null);
        };

        const teardownZoom = () => {
            svg.on('.zoom', null);
        };

        g.selectAll('[data-wesd-node-entity-link]')
            .on('click', function() {
                const refStr = select(this)
                    .attr('data-wesd-node-entity-link');
                const ref = stringToRef(refStr);
                const viewState = kindToViewState(ref.kind);
                $state.go(viewState, {id: ref.id});
            });

        g.selectAll('[data-wesd-node-description]')
            .on('mouseenter', function() {
                const text = select(this)
                    .attr('data-wesd-node-description');

                let top = 0;
                let left = 0;

                select(this)
                    .each((d,i,g) => {
                        const gBounds = g[0].getBoundingClientRect();
                        const holderBounds = holder.getBoundingClientRect();
                        top = gBounds.top - holderBounds.top;
                        left = gBounds.left - holderBounds.left + (gBounds.left > 700 ? -250 : 100);
                    });

                $timeout(() => {
                    vm.popup.style.display = "inline-block";
                    vm.popup.style.left = `${left}px`;
                    vm.popup.style.top = `${top}px`;
                    vm.popup.text = text;
                });
            })
            .on('mouseleave', function() {
                const text = select(this)
                    .attr('data-wesd-node-description');
                $timeout(() => {
                    vm.popup.style.display = "none";
                    vm.popup.text = "";
                });
            });

        vm.zoom = (delta) => {
            currentZoom += delta;
            updateTransform();
        };

        vm.panX = (delta) => {
            currentTranslate[0] += delta;
            updateTransform();
        };

        vm.panY = (delta) => {
            currentTranslate[1] += delta;
            updateTransform();
        };

        vm.togglePanZoom = () => {
            vm.panZoomEnabled = ! vm.panZoomEnabled;
            if (vm.panZoomEnabled) {
                setupZoom();
            } else {
                teardownZoom();
            }
        };
    };

}


controller.$inject = [
    "$element",
    "$state",
    "$timeout"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzEntitySvgDiagramViewer'
};
