<script>
import _ from 'lodash';
import ViewLinkLabelled from "../../../../common/svelte/ViewLinkLabelled.svelte";
import GridWithCellRenderer from "../../../../common/svelte/GridWithCellRenderer.svelte";
import Pill from "../../../../common/svelte/Pill.svelte";
import ProposedFlowFilters from "./ProposedFlowFilters.svelte";
import { filters } from "./filter-store";
import NoData from "../../../../common/svelte/NoData.svelte";
import {getEntityState} from "../../../../common/entity-utils";
import TextClipper from "../../../../common/svelte/TextClipper.svelte";
import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";
import ProposedFlowLink from './ProposedFlowLink.svelte';

export let userName;
export let flows = [];
export let dataTypeIdToNameMap = {};
export let statusPillDefs = {};
export let changeTypePillDefs = {};
export let proposerTypePillDefs = {};
export let actionablePillDefs = {};
export let currentTabText;
export let myActionables = new Map();

const approvalType = {
    SOURCE_APPROVED: "SOURCE_APPROVED",
    TARGET_APPROVED: "TARGET_APPROVED",
    FULLY_APPROVED: "FULLY_APPROVED"
}

const columnDefs = [
    {
        field: "id",
        name: "Flow",
        cellRendererComponent: ProposedFlowLink,
        cellRendererProps: row => ({
            flow: row,
            showExclamation: myActionables.get(row.id),
            currentTab: currentTabText
        }),
        sortable: true,
        width: "10%"
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
            openInNewTab: false,
            isEntityLink: true,
            entityKind: row.flowDef.source?.kind
        }),
        sortable: true,
        width: "5%"
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
            openInNewTab: false,
            isEntityLink: true,
            entityKind: row.flowDef.target.kind
        }),
        sortable: true,
        width: "5%"
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
        cellRenderer: params => `<strong>${statusPillDefs[params.workflowState?.state].name}</strong>`,
        sortable: true
    },
    {
        field: "flowDef.proposalType",
        name: "Change Type",
        cellRenderer: params => `<strong>${changeTypePillDefs[params.flowDef?.proposalType].name}</strong>`,
        sortable: true
    },
    { field: "flowDef.reason.description", name: "Proposal Reason" },
    {
        field: "createdBy",
        name: "Created By",
        sortable: true
    },
    {
        field: "createdAt",
        name: "Created At",
        sortable: true
    },
    {
        field: "sourceApprovedBy",
        name: "Source Approver",
        sortable: true
    },
    {
        field: "sourceApprovedAt",
        name: "Source Approved At",
        sortable: true
    },
    {
        field: "targetApprovedBy",
        name: "Target Approver",
        sortable: true
    },
    {
        field: "targetApprovedAt",
        name: "Target Approved At",
        sortable: true
    }
];

$: activeFilter = $filters[currentTabText];

$: gridData = flows && flows.length
    ? flows.map(flow => {
        let transformedSourceApproved = {at: "", by: ""};
        let transformedTargetApproved = {at: "", by: ""};

        flow.workflowTransitionList?.forEach(t => {
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
        .filter(d => (activeFilter.state.length === 0) || activeFilter.state.includes(d.workflowState.state))
        .filter(d => (activeFilter.change.length === 0) || activeFilter.change.includes(d.flowDef.proposalType))
        .filter(d => (activeFilter.proposer.length === 0) || activeFilter.proposer.includes(d.createdBy === userName ? "USER" : "OTHERS"))
        .filter(d => (activeFilter.action.length === 0) || activeFilter.action.includes(myActionables?.has(d.id) ? "ACTIONABLE" : "ACTIONED"))
        .sort((a, b) => new Date(b.workflowState.lastUpdatedAt) - new Date(a.workflowState.lastUpdatedAt))
        .sort((a, b) => activeFilter.state.indexOf(a.workflowState.state) - activeFilter.state.indexOf(b.workflowState.state))
        .sort((a, b) => activeFilter.change.indexOf(a.flowDef.proposalType) - activeFilter.change.indexOf(b.flowDef.proposalType))
        .sort((a, b) => activeFilter.proposer.indexOf(a.createdBy === userName ? "USER" : "OTHERS")
            - activeFilter.proposer.indexOf(b.createdBy === userName ? "USER" : "OTHERS"))
        .sort((a, b) => activeFilter.action.indexOf(myActionables.has(a.id) ? "ACTIONABLE" : "ACTIONED")
            - activeFilter.action.indexOf(myActionables.has(b.id) ? "ACTIONABLE" : "ACTIONED"))
    : [];

$: stateCounts = _.countBy(gridData, "workflowState.state");
$: changeTypeCounts = _.countBy(gridData, "flowDef.proposalType");
$: proposerCounts = _.countBy(gridData, (row) => row.createdBy === userName ? "USER" : "OTHERS");
$: actionableCounts = _.countBy(gridData, (row) => myActionables.has(row.id) ? "ACTIONABLE" : "ACTIONED");

$: filteredDataSize = filteredGridData.length;

$: isDataFiltered = (!(filteredDataSize === gridData.length) || (activeFilter.change.length || activeFilter.proposer.length || activeFilter.state.length));

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
                                 proposerPillCounts={proposerCounts}
                                 actionablePillDefs={actionablePillDefs}
                                 actionableCounts={actionableCounts}
                                 filterStateKey={currentTabText}/>
            <small class="text-muted">{isDataFiltered ? `Filtered: (${filteredDataSize})` : ``}</small>
            <GridWithCellRenderer columnDefs={columnDefs}
                                  rowData={filteredGridData}
                                  clickable={false}
                                  gridSize={500}/>
        {/if}
    {:else}
        <LoadingPlaceholder/>
    {/if}
</div>