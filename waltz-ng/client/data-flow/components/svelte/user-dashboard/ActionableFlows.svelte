<script>
import _ from 'lodash';
import ViewLinkLabelled from "../../../../common/svelte/ViewLinkLabelled.svelte";
import GridWithCellRenderer from "../../../../common/svelte/GridWithCellRenderer.svelte";
import Pill from "../../../../common/svelte/Pill.svelte";
import ProposedFlowFilters from "./ProposedFlowFilters.svelte";
import { filters } from "./filter-store";
import Icon from "../../../../common/svelte/Icon.svelte";
import NoData from "../../../../common/svelte/NoData.svelte";
import {getEntityState} from "../../../../common/entity-utils";
import TextClipper from "./TextClipper.svelte";
import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";

export let userName;
export let flows = [];
export let dataTypeIdToNameMap = {};

const STATUS_PILL_DEFINITION = {
    PROPOSED_CREATE: {
        name: "Proposed Create",
        color: "#a77a52"
    },
    PENDING_APPROVALS: {
        name: "Pending Approvals",
        color: "#8e8e56"
    },
    FULLY_APPROVED: {
        name: "Fully Approved",
        color: "#5bb65d"
    },
    SOURCE_APPROVED: {
        name: "Source Approved",
        color: "#74a259"
    },
    TARGET_APPROVED: {
        name: "Target Approved",
        color: "#74a259"
    },
    SOURCE_REJECTED: {
        name: "Source Rejected",
        color: "#c1664f"
    },
    TARGET_REJECTED: {
        name: "Target Rejected",
        color: "#c1664f"
    }
}

const CHANGE_TYPE_PILL_DEFINITION = {
    ADD: {
        name: "Create",
        color: "#267dda"
    },
    EDIT: {
        name: "Modify",
        color: "#716b9e"
    },
    REMOVE: {
        name: "Delete",
        color: "#da524b"
    }
}

const PROPOSER_PILL_DEFINITION = {
    USER: {
        name: "You",
        color: "#000000"
    },
    OTHERS: {
        name: "Others",
        color: "#000000"
    }
}

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
            pillDefs: STATUS_PILL_DEFINITION
        })
    },
    {
        field: "changeType",
        name: "Change",
        cellRendererComponent: Pill,
        cellRendererProps: row => ({
            pillKey: row.changeType,
            pillDefs: CHANGE_TYPE_PILL_DEFINITION
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

let stateFilterDefs = _.cloneDeep(STATUS_PILL_DEFINITION);
let changeFilterDefs = _.cloneDeep(CHANGE_TYPE_PILL_DEFINITION);
let proposerFilterDefs = _.cloneDeep(PROPOSER_PILL_DEFINITION);

$: fetch("http://localhost:3456/api/get/prop-flows", {method: "GET"})
    .then(r => r.json())
    .then(r => flows = r)
    .catch(e => flows = []);


$: gridData = flows && flows.length
    ? flows.map(flow => ({
        ...flow,
        dataTypes: flow.proposedFlowCommand.dataTypeIds ? flow.proposedFlowCommand.dataTypeIds.map(id => dataTypeIdToNameMap[id]).join(", ") : ""
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
        <h2>
            <Icon name="envelope-open-o"/>
            Actionable Flows ({filteredGridData.length})
        </h2>
        <small class="text-muted">Data flows that have been proposed to you or those that you may have proposed.</small>
        {#if flows.length === 0}
            <NoData>
                No actionable data flows found for {userName}
            </NoData>
        {:else }
            <ProposedFlowFilters pillDefs={stateFilterDefs}
                                 stateCounts={stateCounts}
                                 changePillDefs={changeFilterDefs}
                                 changeTypeCounts={changeTypeCounts}
                                 proposerPillDefs={proposerFilterDefs}
                                 proposerPillCounts={proposerCounts}/>
            <GridWithCellRenderer columnDefs={columnDefs}
                                  rowData={filteredGridData}/>
        {/if}
    {:else}
        <LoadingPlaceholder/>
    {/if}
</div>