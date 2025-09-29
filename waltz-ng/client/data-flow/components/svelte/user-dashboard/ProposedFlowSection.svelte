<script>
import _ from 'lodash';
import ViewLinkLabelled from "../../../../common/svelte/ViewLinkLabelled.svelte";
import GridWithCellRenderer from "../../../../common/svelte/GridWithCellRenderer.svelte";
import Pill from "../../../../common/svelte/Pill.svelte";
import ProposedFlowFilters from "./ProposedFlowFilters.svelte";
import { filters, tempFilters } from "./filter-store";
import NoData from "../../../../common/svelte/NoData.svelte";
import {getEntityState} from "../../../../common/entity-utils";
import TextClipper from "../../../../common/svelte/TextClipper.svelte";
import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";
import {onMount} from "svelte";

export let userName;
export let flows = [];
export let dataTypeIdToNameMap = {};
export let statusPillDefs = {};
export let changeTypePillDefs = {};
export let proposerTypePillDefs = {};
export let currentTabText;

// works for only two sections if we add another than a new method may be required
onMount(() => {
   const ephemeralFilters = $tempFilters;
   $tempFilters = $filters;
   $filters = ephemeralFilters;
});

const approvalType = {
    SOURCE_APPROVED: "SOURCE_APPROVED",
    TARGET_APPROVED: "TARGET_APPROVED",
    FULLY_APPROVED: "FULLY_APPROVED"
}

const columnDefs = [
    {
        field: "id",
        name: "Flow",
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: "main.proposed-flow.view",
            label: `${row.flowDef.source?.name} → ${row.flowDef.target?.name}`,
            ctx: {
                id: row.id
            },
            openInNewTab: true
        })
    },
    {
        field: "flowDef.source.name",
        name: "Source",
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: getEntityState(row.flowDef.source),
            label: `${row.flowDef.source.name}`,
            ctx: {
                id: row.flowDef.source?.id
            },
            openInNewTab: true,
            isEntityLink: true,
            entityKind: row.flowDef.source?.kind
        })
    },
    {
        field: "flowDef.target.name",
        name: "Target",
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: getEntityState(row.flowDef.target),
            label: `${row.flowDef.target?.name}`,
            ctx: {
                id: row.flowDef.target?.id
            },
            openInNewTab: true,
            isEntityLink: true,
            entityKind: row.flowDef.target.kind
        })
    },
    {
        field: "dataTypes",
        name: "Data Types",
        cellRendererComponent: TextClipper,
        cellRendererProps: row => ({
            text: row.dataTypes
        })
    },
    {
        field: "workflowState.state",
        name: "Status",
        cellRendererComponent: Pill,
        cellRendererProps: row => ({
            pillKey: row.workflowState?.state,
            pillDefs: statusPillDefs
        })
    },
    {
        field: "flowDef.proposalType",
        name: "Change",
        cellRendererComponent: Pill,
        cellRendererProps: row => ({
            pillKey: row.flowDef.proposalType,
            pillDefs: changeTypePillDefs
        })
    },
    { field: "flowDef.reason.description", name: "Proposal Reason" },
    { field: "createdBy", name: "Created By" },
    { field: "createdAt", name: "Created At" },
    { field: "sourceApprovedBy", name: "Source Approver"},
    { field: "sourceApprovedAt", name: "Source Approved At"},
    { field: "targetApprovedBy", name: "Target Approver"},
    { field: "targetApprovedAt", name: "Target Approved At"}
];

$: gridData = flows && flows.length
    ? flows.map(flow => {
        let transformedSourceApproved = {at: "", by: ""};
        let transformedTargetApproved = {at: "", by: ""};

        flow.workflowTransitionList.map(t => {
                if(t.toState === approvalType.SOURCE_APPROVED) {
                    transformedSourceApproved.at = new Date(t.lastUpdatedAt).toLocaleString();
                    transformedSourceApproved.by = t.lastUpdatedBy
                } else if(t.toState === approvalType.TARGET_APPROVED) {
                    transformedTargetApproved.at = new Date(t.lastUpdatedAt).toLocaleString();
                    transformedTargetApproved.by = t.lastUpdatedBy
                }
            }
        );

        return {
        ...flow,
        dataTypes: flow.flowDef?.dataTypeIds?.length !== 0
            ? flow.flowDef.dataTypeIds.map(id => dataTypeIdToNameMap[id]).join(", ")
            : "-",
        createdAt: new Date(flow.createdAt).toLocaleString(),
        sourceApprovedAt: transformedSourceApproved.at,
        sourceApprovedBy: transformedSourceApproved.by,
        targetApprovedAt: transformedTargetApproved.at,
        targetApprovedBy: transformedTargetApproved.by
    }})
    : [];

$: filteredGridData = gridData
    ? gridData
        .filter(d => ($filters.state.length === 0) || $filters.state.includes(d.workflowState.state))
        .filter(d => ($filters.change.length === 0) || $filters.change.includes(d.flowDef.proposalType))
        .filter(d => ($filters.proposer.length === 0) || $filters.proposer.includes(d.createdBy === userName ? "USER" : "OTHERS"))
        .sort((a, b) => $filters.state.indexOf(a.workflowState.state) - $filters.state.indexOf(b.workflowState.state))
        .sort((a, b) => $filters.change.indexOf(a.flowDef.proposalType) - $filters.change.indexOf(b.flowDef.proposalType))
        .sort((a, b) => $filters.proposer.indexOf(a.createdBy === userName ? "USER" : "OTHERS")
            - $filters.proposer.indexOf(b.createdBy === userName ? "USER" : "OTHERS"))
    : [];

$: stateCounts = _.countBy(gridData, "workflowState.state");
$: changeTypeCounts = _.countBy(gridData, "flowDef.proposalType");
$: proposerCounts = _.countBy(gridData, (row) => row.createdBy === userName ? "USER" : "OTHERS");

$: filteredDataSize = filteredGridData.length;

$: isDataFiltered = (!(filteredDataSize === gridData.length) || ($filters.change.length || $filters.proposer.length || $filters.state.length));

</script>

<div>
    {#if flows}
        <small class="text-muted">{currentTabText} data flows that have been proposed to you or those that you may have proposed.</small>
        {#if flows.length === 0}
            <NoData>
                No data flows found for {userName}
            </NoData>
        {:else }
            <ProposedFlowFilters pillDefs={statusPillDefs}
                                 stateCounts={stateCounts}
                                 changePillDefs={changeTypePillDefs}
                                 changeTypeCounts={changeTypeCounts}
                                 proposerPillDefs={proposerTypePillDefs}
                                 proposerPillCounts={proposerCounts}/>
            <small class="text-muted">{isDataFiltered ? `Filtered: (${filteredDataSize})` : ``}</small>
            <GridWithCellRenderer columnDefs={columnDefs}
                                  rowData={filteredGridData}
                                  clickable={false}/>
        {/if}
    {:else}
        <LoadingPlaceholder/>
    {/if}
</div>