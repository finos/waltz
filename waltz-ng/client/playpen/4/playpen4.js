import {event, selectAll, select} from 'd3-selection'

const svg1 = `
   <svg width="400" 
        height="300">
    
        <g id="box">
            <rect id='focus'
                  x="50"
                  y="50"
                  width="140"
                  height="190"
                  stroke="pink"
                  fill="pink">
            </rect>
    
            <rect id='focus'
                  x="10"
                  y="10"
                  width="50"
                  height="50"
                  fill="yellow">
            </rect>
        </g>
    
        <circle cx="100"
                cy="100"
                r="20"
                fill="blue">
        </circle>
    
      
    </svg>
`;


function controller(svgDiagramStore) {

    const vm = Object.assign(this, { svg: svg1 });

    svgDiagramStore.findByKind('ORG_UNIT')
        .then(x => {
            console.log(x);
            return x;
        })

        .then(xs => vm.svg2 = xs[0].svg)
        .then(() => console.log(vm))

    vm.swap = () => vm.svg =  (vm.svg === svg1) ? vm.svg2 : svg1;


}



controller.$inject = [
    'SvgDiagramStore'
];


const view = {
    template: require('./playpen4.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
