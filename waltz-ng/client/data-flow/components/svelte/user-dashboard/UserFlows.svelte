<script>
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {personStore} from "../../../../svelte-stores/person-store";
    import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import GridWithCellRenderer from "../../../../common/svelte/GridWithCellRenderer.svelte";
    import ViewLinkLabelled from "../../../../common/svelte/ViewLinkLabelled.svelte";
    import {getEntityState} from "../../../../common/entity-utils";

    export let userName;

    $: personCall = personStore.getSelf();
    $: person = $personCall?.data;
    $: selectionOptions = {
        entityLifecycleStatuses: ["ACTIVE"],
        entityReference: {
            id: person ? person.id : null,
            kind: person ? person.kind : null
        },
        filters : {},
        scope: "CHILDREN"
    }

    $: logicalFlowCall = person ? logicalFlowStore.findBySelector(selectionOptions) : null;
    $: flows = logicalFlowCall ?
        $logicalFlowCall?.data
        : null;

    const columnDefs = [
        {
            field: "id",
            name: "Link",
            cellRendererComponent: ViewLinkLabelled,
            cellRendererProps: row => ({
                state: "main.logical-flow.view",
                label: "Go To Flow",
                ctx: {
                    id: row.id
                }
            })
        },
        {
            field: "source.name",
            name: "Source",
            cellRendererComponent: ViewLinkLabelled,
            cellRendererProps: row => ({
                state: getEntityState(row.source),
                label: row.source.name,
                ctx: {
                    id: row.source.id
                }
            })
        },
        {
            field: "target.name",
            name: "Target Name",
            cellRendererComponent: ViewLinkLabelled,
            cellRendererProps: row => ({
                state: getEntityState(row.target),
                label: row.target.name,
                ctx: {
                    id: row.target.id
                }
            })
        },
        { field: "created.by", name: "Created By" },
        { field: "provenance", name: "Provenance" },
        { field: "externalId", name: "External Id" },
        { field: "isReadOnly", name: "Read Only" },
        { field: "isRemoved", name: "Removed" }
    ];

    $: rowData = flows && flows.length ?
        flows
        : [];

    const onSelectRow = (row) => {
        console.log(row);
    }
</script>

<div>
    {#if flows}
        {#if flows.length === 0}
            <NoData>
                No data flows found for {userName}
            </NoData>
        {:else}
            <GridWithCellRenderer
                columnDefs={columnDefs}
                rowData={rowData}
                onSelectRow={row => onSelectRow(row)}/>
        {/if}
    {:else }
        <LoadingPlaceholder/>
    {/if}
</div>