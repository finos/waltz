import _ from "lodash";
import {initialiseData} from "../common";
import {green, grey, blue, actor} from "../common/colors";


const template = require('./physical-flow-view.html');


const initialState = {
    bookmarks: [],
    graph: {
        data: {
            flows: [],
            entities: []
        },
        tweakers: {}
    },
    lineage: [],
    lineageExportFn: () => {},
    mentions: [],
    mentionsExportFn: () => {},
    physicalFlow: null,
    selected: {
        entity: null,
        incoming: [],
        outgoing: []
    },
    specification: null,
};


function determineFillColor(d, owningEntity, targetEntity) {
    switch (d.id) {
        case (owningEntity.id): return blue;
        case (targetEntity.id):
            return targetEntity.kind === 'APPLICATION'
                ? green
                : actor;
        default: return grey;
    }
}


function determineRadius(d, owningEntity, targetEntity) {
    switch (d.id) {
        case (owningEntity.id): return 8;
        case (targetEntity.id): return 10;
        default: return 6;
    }
}


function determineStrokeColor(d, owningEntity, targetEntity) {
    switch (d.id) {
        case (owningEntity.id): return blue.darker();
        case (targetEntity.id):
            return targetEntity.kind === 'APPLICATION'
                ? green.darker()
                : actor.darker();
        default: return grey;
    }
}


function setupGraphTweakers(owningEntity, targetEntity, onClick) {
    return {
        node: {
            update: (selection) => {
                selection
                    .select('circle')
                    .attr({
                        'fill': d => determineFillColor(d, owningEntity, targetEntity),
                        'stroke': d => determineStrokeColor(d, owningEntity, targetEntity),
                        'r': d => determineRadius(d, owningEntity, targetEntity)
                    });
            },
            exit: () => {},
            enter: (selection) => {
                selection.on('click.view', onClick);
            }
        }
    };
}


function mkLineageFlows(lineage = []) {
    return _.map(
        lineage,
        (x) => {
            return {
                id: x.flow.id,
                source: x.sourceEntity,
                target: x.targetEntity
            };
        });
}


function mkLineageEntities(lineage = []) {
    return _.chain(lineage)
        .flatMap(
            x => [
                x.sourceEntity,
                x.targetEntity])
        .uniqBy('id')
        .value();
}


function mkFullLineage(lineage = [], flow, spec) {
    if (!flow || !spec) { return lineage; }
    const finalEntry = {
        flow,
        specification: spec,
        sourceEntity: spec.owningEntity,
        targetEntity: flow.target
    };
    return _.concat(lineage, [finalEntry]);
}


function loadBookmarks(bookmarkStore, entityRef) {
    if(!bookmarkStore || !entityRef) return null;
    return bookmarkStore
        .findByParent(entityRef);
}


function controller($q,
                    $scope,
                    $stateParams,
                    bookmarkStore,
                    physicalFlowLineageStore,
                    physicalSpecificationStore,
                    physicalFlowStore)
{
    const vm = initialiseData(this, initialState);

    const flowId = $stateParams.id;

    // -- INTERACT ---
    vm.selectEntity = (entity) => {
        const incoming = _.filter(
            vm.lineage,
            f => f.targetEntity.id === entity.id && f.targetEntity.kind === entity.kind);

        const outgoing =  _.filter(
            vm.lineage,
            f => f.sourceEntity.id === entity.id && f.sourceEntity.kind === entity.kind);

        vm.selected = {
            entity,
            incoming,
            outgoing
        };
    };

    vm.preparePopover = (d) => {
        return `
            <div class="small">
                <table class="table small table-condensed">
                    <tr>
                        <th>Format</th>
                        <td><span>${d.specification.format}</span></td>
                    </tr>
                    <tr>
                        <th>Transport</th>
                        <td><span>${d.flow.transport}</span></td>
                    </tr>
                    <tr>
                        <th>Frequency</th>
                        <td><span>${d.flow.frequency}</span></td>
                    </tr>
                    <tr>
                        <th>Basis Offset</th>
                        <td><span>${d.flow.basisOffset}</span></td>
                    </tr>
                </table>
                <div class="text-muted">
                    ${d.description || ""}
                </div>
            </div>
        `;
    }

    // -- LOAD ---

    const flowPromise = physicalFlowStore
        .getById(flowId);

    const specPromise = flowPromise
        .then(flow => vm.physicalFlow = flow)
        .then(flow => physicalSpecificationStore.getById(flow.specificationId))
        .then(spec => vm.specification = spec);

    specPromise
        .then(() =>  {
            const specRef = {
                kind: 'PHYSICAL_SPECIFICATION',
                id: vm.specification.id
            };
            return loadBookmarks(bookmarkStore, specRef)
        })
        .then(bs => vm.bookmarks = bs);


    const lineagePromise = physicalFlowLineageStore
        .findByPhysicalFlowId(flowId)
        .then(lineage => vm.lineage = lineage);

    $q.all([specPromise, lineagePromise])
        .then(() => {
            const fullLineage = mkFullLineage(vm.lineage, vm.physicalFlow, vm.specification);

            vm.graph = {
                data: {
                    entities: mkLineageEntities(fullLineage),
                    flows: mkLineageFlows(fullLineage)
                },
                tweakers: setupGraphTweakers(
                    vm.specification.owningEntity,
                    vm.physicalFlow.target,
                    (d) => $scope.$applyAsync(() => vm.selectEntity(d)))
            };
        });

    physicalFlowLineageStore
        .findContributionsByPhysicalFlowId(flowId)
        .then(mentions => vm.mentions = mentions);


    vm.onLineagePanelInitialise = (e) => {
        vm.lineageExportFn = e.exportFn;
    };

    vm.exportLineage = () => {
        vm.lineageExportFn();
    };

    vm.onMentionsPanelInitialise = (e) => {
        vm.mentionsExportFn = e.exportFn;
    };

    vm.exportMentions = () => {
        vm.mentionsExportFn();
    };

}


controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'BookmarkStore',
    'PhysicalFlowLineageStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
