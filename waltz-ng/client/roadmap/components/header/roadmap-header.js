import template from "./roadmap-header.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    roadmapId: "<"
};


const modes = {
    LOADING: "LOADING",
    VIEW: "VIEW"
};


const initialState = {
    modes,
    ratingScheme: null,
    roadmap: null,
    visibility: {
        mode: modes.LOADING
    }
};


const addToHistory = (historyStore, roadmap) => {
    if (! roadmap) { return; }
    historyStore.put(
        roadmap.name,
        "ROADMAP",
        "main.roadmap.view",
        { id: roadmap.id });
};



function controller($q,
                    historyStore,
                    serviceBroker,
                    notification)
{
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.visibility.mode = modes.LOADING;
        reloadAllData()
            .then(() => {
                vm.visibility.mode = modes.VIEW;
                addToHistory(historyStore, vm.roadmap);
            });
    };


    vm.onSaveRoadmapName = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.RoadmapStore.updateName,
                data,
                true,
                "Roadmap name updated")
            .then(() => reloadAllData());
    };

    vm.onSaveRoadmapDescription = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.RoadmapStore.updateDescription,
                data,
                false,
                "Roadmap description updated")
            .then(() => reloadAllData());
    };

    vm.onCancel = () => {
        vm.visibility.mode = modes.LIST;
        vm.selectedScenario = null;
        reloadAllData();
    };

    vm.markActive = () => {
        return updateField(
            vm.roadmap.id,
            CORE_API.RoadmapStore.updateLifecycleStatus,
            {
                newVal: 'ACTIVE',
                oldVal: vm.roadmap.entityLifecycleStatus
            },
            true,
            "Roadmap restored")
            .then(() => reloadAllData());
    };

    vm.markRemoved = () => {
        return updateField(
            vm.roadmap.id,
            CORE_API.RoadmapStore.updateLifecycleStatus,
            {
                newVal: 'REMOVED',
                oldVal: vm.roadmap.entityLifecycleStatus
            },
            true,
            "Roadmap deleted")
            .then(() => reloadAllData());
    };

    // -- helpers --

    function updateField(roadmapId,
                         method,
                         data,
                         preventNull = true,
                         message = "Updated") {
        if (preventNull && (_.isEmpty(data.newVal) && !_.isDate(data.newVal))) {
            return Promise.reject("Waltz:updateField - Cannot set an empty value");
        }
        if (data.newVal !== data.oldVal) {
            return serviceBroker
                .execute(
                    method,
                    [ roadmapId, data.newVal ])
                .then(() => notification.success(message));
        } else {
            return Promise.reject("Nothing updated")
        }
    }


    function reloadAllData() {
        const roadmapPromise = serviceBroker
            .loadViewData(
                CORE_API.RoadmapStore.getRoadmapById,
                [ vm.roadmapId ],
                { force: true })
            .then(r => vm.roadmap = r.data);

        return $q
            .all([roadmapPromise])
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.RatingSchemeStore.getById,
                    [vm.roadmap.ratingSchemeId]
                )
                .then(r => vm.ratingScheme = r.data));
    }

}


controller.$inject = [
    "$q",
    "HistoryStore",
    "ServiceBroker",
    "Notification"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzRoadmapHeader",
    component
};