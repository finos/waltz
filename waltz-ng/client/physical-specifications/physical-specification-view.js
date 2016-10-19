import {initialiseData} from '../common';


const template = require('./physical-specification-view.html');


const initialState = {
    visibility: {
        createReportOverlay: false,
        createReportButton: true,
        createReportBusy: false
    },
    createReportForm: {
        name: ""
    },
    selectedFlow: null
};



function controller($state,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
                    lineageReportStore,
                    logicalDataFlowStore,
                    notification,
                    orgUnitStore,
                    physicalSpecificationStore,
                    physicalFlowStore)
{
    const vm = initialiseData(this, initialState);

    const specId = $stateParams.id;
    const ref = {
        kind: 'PHYSICAL_SPECIFICATION',
        id: specId
    };

    // -- LOAD ---

    physicalSpecificationStore
        .getById(specId)
        .then(spec => vm.specification = spec)
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningEntity = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou);

    physicalFlowStore
        .findBySpecificationId(specId)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);


    bookmarkStore
        .findByParent(ref)
        .then(bs => vm.bookmarks = bs);


    // -- INTERACT ---

    vm.openCreateReportPopup = () => {
        vm.visibility.createReportOverlay = ! vm.visibility.createReportOverlay;
        if (!vm.createReportForm.name) {
            vm.createReportForm.name = vm.spec.name + " Lineage Report";
        }
    };

    vm.canCreateReport = () => {
        const name = vm.createReportForm.name || "";
        return name.length > 1;
    };

    vm.createReport = () => {
        vm.visibility.createReportButton = false;
        vm.visibility.createReportBusy = true;

        lineageReportStore
            .create({ name: vm.createReportForm.name, specificationId: vm.specification.id })
            .then(reportId => {
                notification.success("Lineage Report created");
                $state.go('main.lineage-report.edit', { id: reportId });
                vm.visibility.createReportButton = true;
                vm.visibility.createReportBusy = false;
            });
    };

    vm.onFlowSelect = (flow) => {
        console.log("ofs", flow);
        vm.selectedFlow = {
            flow,
            mentions: [],
            lineage: []
        }
    }

}


controller.$inject = [
    '$state',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'LineageReportStore',
    'DataFlowDataStore', // LogicalDataFlowStore
    'Notification',
    'OrgUnitStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};