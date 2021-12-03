<script>

    import ReportGridPicker from "./ReportGridPicker.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {selectedGrid} from "./report-grid-store";
    import Icon from "../../../common/svelte/Icon.svelte";

    const Modes = {
    VIEW: "VIEW",
    PICKER: "PICKER",
}

export let onGridSelect = () => console.log("selecting grid");
let activeMode = Modes.VIEW;

function selectGrid(grid) {
    onGridSelect(grid);
    activeMode = Modes.VIEW;
}

function cancel() {
    activeMode = Modes.VIEW
}

</script>

{#if activeMode === Modes.VIEW}
    {#if $selectedGrid}
        <h4>{$selectedGrid.definition.name}</h4>
        <p>{$selectedGrid.definition.description}</p>
    {:else}
        <NoData>No grid selected</NoData>
    {/if}
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.PICKER}>
        <Icon name="random"/>Change grid
    </button>
{:else if activeMode === Modes.PICKER}
    <ReportGridPicker onGridSelect={selectGrid} onCancel={cancel}/>
{/if}
