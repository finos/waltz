import _ from "lodash";
import {sameRef} from "../../../../common/entity-utils";
import {cmp} from "../../../../common/sort-utils";
import tippy from "tippy.js";
import {
    toCriticalityName,
    toFrequencyKindName, toTransportKindName
} from "../propose-data-flow/propose-data-flow-utils";
import {entity as EntityKind} from "../../../../common/services/enums/entity";

export const Directions = {
    INBOUND: "INBOUND",
    OUTBOUND: "OUTBOUND",
    ALL: "ALL"
};

export const FlowNodeTypes = [
    EntityKind.ACTOR,
    EntityKind.APPLICATION,
    EntityKind.END_USER_APPLICATION
];

function determineDirection(flow, parentEntityRef) {
    if (sameRef(flow.target, parentEntityRef)) {
        return Directions.INBOUND;
    } else {
        return Directions.OUTBOUND;
    }
}


function groupRatingsByDefId(assessmentRatingsForLogicalFlow, ratingSchemeItemsById) {
    return _
        .chain(assessmentRatingsForLogicalFlow)
        .groupBy(r => r.assessmentDefinitionId)
        .mapValues(v => _
            .chain(v)
            .map(r => ratingSchemeItemsById[r.ratingId])
            .filter(d => d != null)
            .sortBy(r => r.position, r => r.name)
            .value())
        .value();
}


export function mkFlowDetails(flowView, parentEntityRef) {

    const logicalDecoratorsByFlowId = _.groupBy(flowView.logicalFlowDataTypeDecorators, d => d.dataFlowId);
    const specDecoratorsBySpecId = _.groupBy(flowView.physicalSpecificationDataTypeDecorators, d => d.dataFlowId);
    const logicalFlowRatingsByFlowId = _.groupBy(flowView.logicalFlowRatings, d => d.entityReference.id);
    const physicalFlowRatingsByFlowId = _.groupBy(flowView.physicalFlowRatings, d => d.entityReference.id);
    const physicalSpecRatingsByFlowId = _.groupBy(flowView.physicalSpecificationRatings, d => d.entityReference.id);
    const ratingSchemeItemsById = _.keyBy(flowView.ratingSchemeItems, d => d.id);
    const specsById = _.keyBy(flowView.physicalSpecifications, d => d.id);
    const physicalFlowsByLogicalFlowId = _.groupBy(flowView.physicalFlows, d => d.logicalFlowId);

    return _
        .chain(flowView.logicalFlows)
        .flatMap(d => {
            const physicalFlows = _.get(physicalFlowsByLogicalFlowId, d.id, []);
            return _.isEmpty(physicalFlows)
                ? [{logicalFlow: d, physicalFlow: null}]
                : _.map(physicalFlows, p => ({logicalFlow: d, physicalFlow: p}))
        })
        .map(t => {
            const logicalFlow = t.logicalFlow;
            const physicalFlow = t.physicalFlow;

            const assessmentRatingsForLogicalFlow = _.get(logicalFlowRatingsByFlowId, logicalFlow.id, []);
            const assessmentRatingsForPhysicalFlow = _.get(physicalFlowRatingsByFlowId, physicalFlow?.id, []);
            const assessmentRatingsForPhysicalSpec = _.get(physicalSpecRatingsByFlowId, physicalFlow?.specificationId, []);
            const dataTypesForLogicalFlow = _.get(logicalDecoratorsByFlowId, logicalFlow.id, []);
            const specification = _.get(specsById, physicalFlow?.specificationId);
            const dataTypesForSpecification = specification
                ? _.get(specDecoratorsBySpecId, specification.id, [])
                : [];

            const logicalFlowRatingsByDefId = groupRatingsByDefId(assessmentRatingsForLogicalFlow, ratingSchemeItemsById);
            const physicalFlowRatingsByDefId = groupRatingsByDefId(assessmentRatingsForPhysicalFlow, ratingSchemeItemsById);
            const physicalSpecRatingsByDefId = groupRatingsByDefId(assessmentRatingsForPhysicalSpec, ratingSchemeItemsById);

            const direction = determineDirection(logicalFlow, parentEntityRef);

            return {
                direction,
                logicalFlow,
                physicalFlow,
                specification,
                dataTypesForLogicalFlow,
                dataTypesForSpecification,
                logicalFlowRatingsByDefId,
                physicalFlowRatingsByDefId,
                physicalSpecRatingsByDefId,
                allRatings: _.concat(
                    assessmentRatingsForLogicalFlow,
                    assessmentRatingsForPhysicalFlow,
                    assessmentRatingsForPhysicalSpec)
            };
        })
        .sortBy([
            d => d.logicalFlow.target.name,
            d => d.logicalFlow.source.name
        ])
        .value();
}

const sourceNameCol = {
    id: "source_name",
    name: "Source",
    field: "logicalFlow",
    sortable:  true,
    width: 150,
    formatter: (row, cell, value, colDef, dataCtx) => value.source.name,
    sortFn: (a, b) => cmp(a?.logicalFlow.source.name, b?.logicalFlow.source.name)
};

const sourceExtIdCol = {
    id: "source_ext_id",
    name: "Src Ext Id",
    field: "logicalFlow",
    sortable:  true,
    formatter: (row, cell, value, colDef, dataCtx) => value.source.externalId,
    sortFn: (a, b) => cmp(a?.logicalFlow.source.externalId, b?.logicalFlow.source.externalId)
};

const targetNameCol = {
    id: "target_name",
    name: "Target",
    field: "logicalFlow",
    sortable:  true,
    width: 150,
    formatter: (row, cell, value, colDef, dataCtx) => value.target.name,
    sortFn: (a, b) => cmp(a?.logicalFlow.target.name, b?.logicalFlow.target.name)
};

const targetExtIdCol = {
    id: "target_ext_id",
    name: "Trg Ext Id",
    field: "logicalFlow",
    sortable:  true,
    formatter: (row, cell, value, colDef, dataCtx) => value.target.externalId,
    sortFn: (a, b) => cmp(a?.logicalFlow.target.externalId, b?.logicalFlow.target.externalId)
};


const flowExtIdCol = {
    id: "flow_ext_id",
    name: "Flow Ext Id",
    field: "logicalFlow",
    sortable:  true,
    formatter: (row, cell, value, colDef, dataCtx) => value.externalId,
    sortFn: (a, b) => cmp(a?.logicalFlow.externalId, b?.logicalFlow.externalId)
};


const physicalNameCol = {
    id: "physical_name",
    name: "Name",
    sortable: true,
    width: 170,
    formatter: (row, cell, value, colDef, dataCtx) => dataCtx.physicalFlow.name || dataCtx.specification?.name || ""
};

const physicalExtIdCol = {
    id: "physical_ext_id",
    name: "Ext Id",
    sortable: true,
    field: "physicalFlow",
    width: 170,
    formatter: (row, cell, value, colDef, dataCtx) => value.externalId || ""
};


function mkCriticalityCol(nestedEnums) {
    return {
        id: "criticality",
        name: "Criticality",
        field: "physicalFlow",
        sortable: false,
        width: 120,
        formatter: (row, cell, value, colDef, dataCtx) => toCriticalityName(nestedEnums, value.criticality)
    };
}


function mkFrequencyCol(nestedEnums) {
    return {
        id: "frequency",
        name: "Frequency",
        field: "physicalFlow",
        sortable: false,
        width: 120,
        formatter: (row, cell, value, colDef, dataCtx) => toFrequencyKindName(nestedEnums, value.frequency)
    };
}


function mkTransportCol(nestedEnums) {
    return {
        id: "transport",
        name: "Transport",
        field: "physicalFlow",
        sortable: false,
        width: 120,
        formatter: (row, cell, value, colDef, dataCtx) => toTransportKindName(nestedEnums, value.transport)
    };
}


function mkDataTypesCol(fieldName) {
    return {
        id: "data_types",
        name: "Data Types",
        field: fieldName,
        sortable: false,
        width: 170,
        formatter: (row, cell, value, colDef, dataCtx) => _
            .chain(value)
            .map(d => d.decoratorEntity.name)
            .sort()
            .join(", ")
            .value()
    };
}


const physicalFlowIndicatorCol = {
    id: "phys_flow_indicator",
    name: "",
    field: "physicalCount",
    sortable:  true,
    width: 16,
    formatter: (row, cell, value, colDef, dataCtx) => {
        switch (value) {
            case 0:
                return "<i class='fa fa-fw'/>";
            case 1:
                return `<i title='1 associated physical flow'
                           class='fa fa-fw fa-file-o'/>`;
            default:
                return `<i title='${value} associated physical flows'
                           class='fa fa-fw fa-folder-o'/>`;
        }
    },
    sortFn: (a, b) => cmp(a?.logicalFlow.source.name, b?.logicalFlow.source.name)
};


function mkLogicalFlowAssessmentColumns(defs) {
    return _.map(defs, d => {
        return {
            id: "assessment_definition/" + d.id,
            assessmentDefinitionId: d.id,
            name: d.name,
            field: "logicalFlowRatingsByDefId",
            sortable: false,
            width: 120,
            formatter: (row, cell, value, colDef, dataCtx) => _
                .chain(value)
                .get(colDef.assessmentDefinitionId, [])
                .map(d => `<span title='${d.description}' style="border-left: solid 2px ${d.color}; padding-left: 0.2em">${d.name}</span>`)
                .join(", ")
                .value()
        }
    });
}


function mkPhysicalFlowAssessmentColumns(defs) {
    return _.map(defs, d => {
        return {
            id: "assessment_definition/" + d.id,
            assessmentDefinitionId: d.id,
            name: d.name,
            sortable: false,
            width: 120,
            formatter: (row, cell, value, colDef, dataCtx) => {
                const ratingsByDefId = _.merge(dataCtx.physicalSpecRatingsByDefId, dataCtx.physicalFlowRatingsByDefId);
                return _
                    .chain(ratingsByDefId)
                    .get(colDef.assessmentDefinitionId, [])
                    .map(d => `<span title='${d.description}' style="border-left: solid 2px ${d.color}; padding-left: 0.2em">${d.name}</span>`)
                    .join(", ")
                    .value()
            }
        }
    });
}


export function mkLogicalFlowTableColumns(defs = []) {
    return _.concat(
        [
            physicalFlowIndicatorCol,
            sourceNameCol,
            sourceExtIdCol,
            targetNameCol,
            targetExtIdCol,
            flowExtIdCol,
            mkDataTypesCol("dataTypesForLogicalFlow")
        ],
        mkLogicalFlowAssessmentColumns(defs));
}


export function mkPhysicalFlowTableColumns(defs = [], nestedEnums = {}) {
    return _.concat(
        [
            sourceNameCol,
            sourceExtIdCol,
            targetNameCol,
            targetExtIdCol,
            physicalNameCol,
            physicalExtIdCol,
            mkDataTypesCol("dataTypesForSpecification"),
            mkCriticalityCol(nestedEnums),
            mkFrequencyCol(nestedEnums),
            mkTransportCol(nestedEnums)
        ],
        mkPhysicalFlowAssessmentColumns(defs));
}




function mkDataTypeTooltipTable(dataTypes, flowClassificationsByCode = null) {
    const mkRatingIcon = (color) => color
        ? `<div class="rating-icon"
                    style='
                        display: inline-block;
                        height: 1em;
                        width: 1em;
                        border:1px solid #ccc;
                        border-radius: 2px;
                        background-color: ${color}'>
                </div>`
        : "";

    const mkClassificationCell = (classification) => {
        const name = _.get(classification, ["name"]);
        const color = _.get(classification, ["color"], "None");
        return `
                <span style='opacity: ${name ? 1 : 0.4}'>
                    ${ mkRatingIcon(color)}
                    ${name ? name : 'none'}
                </span>`;
    };

    const rows = _
        .chain(dataTypes)
        .map(d => ({name: d.decoratorEntity.name, outboundRatingCode: d.rating, inboundRatingCode: d.targetInboundRating}))
        .map(d => `
                <tr>
                    <td>${d.name}</td>
                    <td>${ mkClassificationCell(_.get(flowClassificationsByCode, [d.outboundRatingCode]))}</td>
                    <td>${ mkClassificationCell(_.get(flowClassificationsByCode, [d.inboundRatingCode]))}</td>
                </tr>`)
        .join("\n")
        .value();

    return `
        <table class="table table-condensed small">
            <thead>
                <tr>
                    <th>Data Type</th>
                    <th>Source Classification</th>
                    <th>Consumer Classification</th>
                </tr>
            </thead>
            <tbody class="">${rows}</tbody>
        </table>`
}


export function showDataTypeTooltip(cellElem, dataTypes, flowClassificationsByCode = null) {
    const tippyConfig = {
        content: mkDataTypeTooltipTable(
            dataTypes,
            flowClassificationsByCode),
        delay: [300, 100],
        interactive: true,
        allowHTML: true,
        arrow: true,
        appendTo: document.body,
        theme: "light-border"
    };
    tippy(cellElem, tippyConfig);
}
