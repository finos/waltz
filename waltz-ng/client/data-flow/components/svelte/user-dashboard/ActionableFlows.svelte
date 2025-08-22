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

export let userName;
export let flows = [];
export let dataTypeIdToNameMap = {};
export let statusPillDefs = {};
export let changeTypePillDefs = {};
export let proposerTypePillDefs = {};

const columnDefs = [
    {
        field: "proposedFlowId",
        name: "Flow",
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: "main.logical-flow.view",
            label: `${row.proposedFlowCommand.source.name} → ${row.proposedFlowCommand.target.name}`,
            ctx: {
                id: row.proposedFlowId
            },
            openInNewTab: true
        })
    },
    {
        field: "proposedFlowCommand.source.name",
        name: "Source",
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: getEntityState(row.proposedFlowCommand.source),
            label: `${row.proposedFlowCommand.source.name}`,
            ctx: {
                id: row.proposedFlowCommand.source.id
            },
            openInNewTab: true,
            isEntityLink: true,
            entityKind: row.proposedFlowCommand.source.kind
        })
    },
    {
        field: "proposedFlowCommand.target.name",
        name: "Target",
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: getEntityState(row.proposedFlowCommand.target),
            label: `${row.proposedFlowCommand.target.name}`,
            ctx: {
                id: row.proposedFlowCommand.target.id
            },
            openInNewTab: true,
            isEntityLink: true,
            entityKind: row.proposedFlowCommand.target.kind
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
        field: "status",
        name: "Status",
        cellRendererComponent: Pill,
        cellRendererProps: row => ({
            pillKey: row.status,
            pillDefs: statusPillDefs
        })
    },
    {
        field: "changeType",
        name: "Change",
        cellRendererComponent: Pill,
        cellRendererProps: row => ({
            pillKey: row.changeType,
            pillDefs: changeTypePillDefs
        })
    },
    { field: "proposedFlowCommand.reason.description", name: "Proposal Reason" },
    { field: "createdBy", name: "Created By" },
    { field: "created_at", name: "Created At" },
    { field: "sourceApprovedBy", name: "Source Approver"},
    { field: "sourceApprovedAt", name: "Source Approved At"},
    { field: "targetApprovedBy", name: "Target Approver"},
    { field: "targetApprovedAt", name: "Target Approved At"}
];

$: gridData = flows && flows.length
    ? flows.map(flow => ({
        ...flow,
        dataTypes: flow.proposedFlowCommand.dataTypeIds
            ? flow.proposedFlowCommand.dataTypeIds.map(id => dataTypeIdToNameMap[id]).join(", ")
            : ""
    }))
    : [];

$: filteredGridData = gridData
    ? gridData
        .filter(d => ($filters.state.length === 0) || $filters.state.includes(d.status))
        .filter(d => ($filters.change.length === 0) || $filters.change.includes(d.changeType))
        .filter(d => ($filters.proposer.length === 0) || $filters.proposer.includes(d.createdBy === userName ? "USER" : "OTHERS"))
        .sort((a, b) => $filters.state.indexOf(a.status) - $filters.state.indexOf(b.status))
        .sort((a, b) => $filters.change.indexOf(a.changeType) - $filters.change.indexOf(b.changeType))
        .sort((a, b) => $filters.proposer.indexOf(a.createdBy === userName ? "USER" : "OTHERS")
            - $filters.proposer.indexOf(b.createdBy === userName ? "USER" : "OTHERS"))
    : [];

$: stateCounts = _.countBy(gridData, "status");
$: changeTypeCounts = _.countBy(gridData, "changeType");
$: proposerCounts = _.countBy(gridData, (row) => row.createdBy === userName ? "USER" : "OTHERS");

</script>

<div>
    {#if flows}
        <small class="text-muted">Data flows that have been proposed to you or those that you may have proposed.</small>
        {#if flows.length === 0}
            <NoData>
                No actionable data flows found for {userName}
            </NoData>
        {:else }
            <ProposedFlowFilters pillDefs={statusPillDefs}
                                 stateCounts={stateCounts}
                                 changePillDefs={changeTypePillDefs}
                                 changeTypeCounts={changeTypeCounts}
                                 proposerPillDefs={proposerTypePillDefs}
                                 proposerPillCounts={proposerCounts}/>
            <GridWithCellRenderer columnDefs={columnDefs}
                                  rowData={filteredGridData}/>
        {/if}
    {:else}
        <LoadingPlaceholder/>
    {/if}
</div>