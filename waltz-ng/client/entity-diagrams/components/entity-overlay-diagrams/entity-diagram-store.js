import {derived, get, writable} from "svelte/store";
import _ from "lodash";
import {defaultOverlay} from "./entity-diagram-utils";
import {$http} from "../../../common/WaltzHttp";
import {overlayDiagramKind} from "../../../common/services/enums/overlay-diagram-kind";
import {buildHierarchies, flattenChildren} from "../../../common/hierarchy-utils";
import {toEntityRef} from "../../../common/entity-utils";

export let selectionOptions = writable(null);
const selectedDiagram = writable(null);

const groups = writable([]);
const groupData = writable([]);

const overlayData = writable([]);
const overlayProperties = writable({});

const selectedGroup = writable(null);
export let hoveredGroupId = writable(null);

const selectedOverlay = writable(defaultOverlay);

const groupsWithData = derived(
    [groups, groupData, overlayData],
    ([$groups, $groupData, $overlayData]) => {
        const dataByGroupId = _.keyBy($groupData, d => d.cellId);
        return _
            .chain($groups)
            .map(g => Object.assign(g, { data: dataByGroupId[g.id], overlayData: $overlayData[g.id] })) // lookup data or fallback to any value set on the input
            .orderBy(d => d.position || d.id)
            .value();
    });

const diagramLayout = derived([groupsWithData], ([$groupsWithData]) => _.first(buildHierarchies($groupsWithData)));

function _loadOverlayData() {

    const diagram = get(selectedDiagram);
    const opts = get(selectionOptions);
    const overlay = get(selectedOverlay);

    if(diagram && overlay && opts) {
        const body = Object.assign(
            {},
            {
                idSelectionOptions: opts,
                overlayParameters: {}
            });

        return $http
            .post(`api/aggregate-overlay-diagram/diagram-id/${diagram.id}/${overlay.url}`, body)
            .then(d => {

                const allOverlayData = d.data;

                const cellDataByExtId = _.keyBy(allOverlayData.cellData, d => d.cellExternalId);

                const props = overlay.mkGlobalProps
                    ? overlay.mkGlobalProps(allOverlayData)
                    : {};

                overlayData.set(cellDataByExtId);
                overlayProperties.set(props)
            });
    }
}


function selectOverlay(overlay) {
    selectedOverlay.set(overlay);
    _loadOverlayData();
}

function saveDiagram(diagram) {

    const layoutData = JSON.stringify(get(groups));
    const backingEntities = get(groupData);

    const createCmd = Object.assign(
        {},
        diagram,
        {
            layoutData,
            backingEntities,
            diagramKind: overlayDiagramKind.WALTZ_ENTITY_OVERLAY.key
        });

    return $http
        .post("api/aggregate-overlay-diagram/create", createCmd);
}


function selectDiagram(diagramId) {

    reset();

    return $http
        .get(`api/aggregate-overlay-diagram/id/${diagramId}`)
        .then(d => {
            const diagramInfo = d.data;
            selectedDiagram.set(diagramInfo.diagram);
            groups.set(JSON.parse(diagramInfo.diagram.layoutData));
            groupData.set(diagramInfo.backingEntities);
            selectedGroup.set(null);
            selectedOverlay.set(defaultOverlay);
            _loadOverlayData();
            return d.data;
        });
}


function selectGroup(group) {
    selectedGroup.set(group);
}

function uploadDiagramLayout(layoutData = []) {

    reset();

    const data = _
        .chain(layoutData)
        .filter(d => !_.isEmpty(d.data))
        .map(d => {
            const cellId = d.id;
            const ref = d.data.entityReference || toEntityRef(d.data);
            return ({cellId: cellId, entityReference: ref});
        })
        .value();

    groups.set(layoutData);
    groupData.set(data);
}


function addGroup(newGroup, data) {
    groups.update((gs) => {
        return _.concat(gs, newGroup)
    });

    if(!_.isEmpty(data)) {
        groupData.update((gd) => {
            const ref = data.entityReference || toEntityRef(data);
            const backingEntity = Object.assign({}, {cellId: newGroup.id, entityReference: ref})
            return _.concat(gd, backingEntity);
        });
    }
}


function removeGroup(group) {
    groups.update((gs) => {
        const children = flattenChildren(group);
        const groupsToRemove = _.concat(children, group);
        return  _.reject(gs, d => _.includes(_.map(groupsToRemove, d => d.id), d.id));
    })
    selectedGroup.set(null);
}

function updateGroup(group) {
    groups.update((gs) => {
        const withoutGroup = _.reject(gs, d => d.id === group.id);
        return _.concat(withoutGroup, group);
    })
    selectedGroup.set(group);
}

function updateChildren(parentGroupId, childGroups) {
    groups.update((gs) => {
        const withoutGroup = _.reject(gs, d => d.parentId === parentGroupId);
        return _.concat(withoutGroup, ...childGroups);
    })
}

function reset() {
    selectedDiagram.set(null);
    groups.set([]);
    groupData.set([]);
    selectedGroup.set(null);
    selectedOverlay.set(defaultOverlay);
}


function createStores() {

    return {
        selectedDiagram: {subscribe: selectedDiagram.subscribe},
        groups: {subscribe: groups.subscribe},
        groupData: {subscribe: groupData.subscribe},
        groupsWithData: {subscribe: groupsWithData.subscribe},
        selectedGroup: {subscribe: selectedGroup.subscribe},
        selectedOverlay: {subscribe: selectedOverlay.subscribe},
        diagramLayout: {subscribe: diagramLayout.subscribe},
        overlayData: {subscribe: overlayData.subscribe},
        overlayProperties: {subscribe: overlayProperties.subscribe},
        selectDiagram,
        saveDiagram,
        uploadDiagramLayout,
        selectGroup,
        addGroup,
        removeGroup,
        updateGroup,
        updateChildren,
        selectOverlay,
        reset,
    };
}

export const diagramService = createStores();