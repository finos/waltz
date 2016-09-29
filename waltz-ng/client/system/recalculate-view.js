const template = require('./recalculate-view.html');


function controller(notification,
                    authSourceStore,
                    complexityStore,
                    dataTypeUsageStore) {
    const vm = this;


    vm.recalcFlowRatings = () => {
        notification.info('Flow Ratings recalculation requested');
        authSourceStore
            .recalculateAll()
            .then(() => notification.success('Flow Ratings recalculated'));
    };

    vm.recalcDataTypeUsages = () => {
        notification.info('Data Type Usage recalculation requested');
        dataTypeUsageStore
            .recalculateAll()
            .then(() => notification.success('Data Type Usage recalculated'));
    };

    vm.recalcComplexity = () => {
        notification.info('Complexity recalculation requested');
        complexityStore
            .recalculateAll()
            .then(() => notification.success('Complexity recalculated'));
    }
}


controller.$inject = [
    'Notification',
    'AuthSourcesStore',
    'ComplexityStore',
    'DataTypeUsageStore'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;