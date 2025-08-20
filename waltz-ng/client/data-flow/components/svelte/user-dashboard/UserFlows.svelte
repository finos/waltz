<script>
    import LoadingPlaceholder from "../../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import GridWithCellRenderer from "../../../../common/svelte/GridWithCellRenderer.svelte";
    import ViewLinkLabelled from "../../../../common/svelte/ViewLinkLabelled.svelte";
    import {getEntityState} from "../../../../common/entity-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let userName;
    export let flows;

    const columnDefs = [
        {
            field: "id",
            name: "Logical Flow",
            cellRendererComponent: ViewLinkLabelled,
            cellRendererProps: row => ({
                state: "main.logical-flow.view",
                label: `${row.source.name} → ${row.target.name}`,
                ctx: {
                    id: row.id
                },
                openInNewTab: true
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
                },
                openInNewTab: true,
                isEntityLink: true,
                entityKind: row.source.kind
            })
        },
        {
            field: "target.name",
            name: "Target",
            cellRendererComponent: ViewLinkLabelled,
            cellRendererProps: row => ({
                state: getEntityState(row.target),
                label: row.target.name,
                ctx: {
                    id: row.target.id
                },
                openInNewTab: true,
                isEntityLink: true,
                entityKind: row.target.kind
            })
        },
        { field: "created.by", name: "Created By" },
        { field: "provenance", name: "Provenance" },
        { field: "externalId", name: "External Id" }
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
        <h2>
            <Icon name="handshake-o"/>
            Involved Flows ({flows.length})
        </h2>
        <small class="text-muted">Data flows of applications that you are involved in.</small>
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