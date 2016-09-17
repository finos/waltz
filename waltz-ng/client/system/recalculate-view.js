const template = require('./recalculate-view.html');


function controller(notification, authSourceStore, dataTypeUsageStore) {
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
}


controller.$inject = [
    'Notification',
    'AuthSourcesStore',
    'DataTypeUsageStore'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;