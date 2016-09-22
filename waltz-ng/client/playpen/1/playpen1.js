
const initData = {
    apps: [],
    flowData: null
};


const steps = [
    {
        //element: '#step1 div',
        intro: 'General blurb.',
        // position: 'top'
    }, {
        element: '#step1 div',
        intro: 'More features, more fun.',
        position: 'top'
    }, {
        element: '#step2 div',
        intro: 'Blah de blah',
        position: 'top'
    }, {
        element: '#step3 div',
        intro: 'Lah lah lah ',
        position: 'top'
    }, {
        element: '#step4 div',
        intro: 'Ho hom',
        position: 'top'
    }
];


function controller(appStore, flowViewService, tourService) {

    const vm = Object.assign(this, initData);

    //
    tourService.initialiseWithSteps(steps);

    const entityReference = {
        id: 170,
        kind: 'ORG_UNIT'
    };

    const selector = {
        entityReference,
        scope: 'CHILDREN'
    };

    appStore
        .findBySelector(selector)
        .then(apps => vm.applications = apps);

    flowViewService
        .initialise(selector)
        .then(flowViewService.loadDetail)
        .then(d => vm.flowData = d);

    vm.loadDetail = () => {};

    vm.options = {
        graphTweakers: {
            node: {
                enter: (selection) => selection.on('click.app-click', d => console.log(d)),
                update: _.identity,
                exit: _.identity
            }
        }
    };

    vm.startTour = () => {
        tourService.start();
    };
}


controller.$inject = [
    'ApplicationStore',
    'DataFlowViewService',
    'TourService'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;