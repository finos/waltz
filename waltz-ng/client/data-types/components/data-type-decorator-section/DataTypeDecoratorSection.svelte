<script>

    import DataTypeOverviewPanel from "./DataTypeOverviewPanel.svelte";
    import DataTypeDetailContextPanel from "./context-panel/DataTypeDetailContextPanel.svelte";
    import Toggle from "../../../common/svelte/Toggle.svelte";
    import DataTypeDecoratorViewGrid from "./DataTypeDecoratorViewGrid.svelte";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {
        selectedDecorator,
        viewData,
        enrichedDecorators,
        selectedDataType
    } from "./data-type-decorator-section-store";
    import {prepareData} from "./data-type-decorator-view-grid-utils";

    const Modes = {
        TREE: "TREE",
        TABLE: "TABLE"
    }

    export let primaryEntityRef;

    let activeMode = Modes.TREE;
    let viewCall;

    function toggleView() {
        if (activeMode === Modes.TREE) {
            activeMode = Modes.TABLE;
        } else {
            activeMode = Modes.TREE;
        }
    }

    $: {
        if (primaryEntityRef) {
            viewCall = dataTypeDecoratorStore.getViewForParentRef(primaryEntityRef);
        }
    }

    $: $viewData = $viewCall?.data;

    $: {
        if ($viewData) {
            $enrichedDecorators = prepareData($viewData);
        }
    }

</script>

<div class="decorator-section">
    <div class="decorator-table">
        <div class="pull-right" style="display: block">
            <Toggle labelOn="Tree View"
                    labelOff="Table View"
                    state={activeMode === Modes.TREE}
                    onToggle={toggleView}/>
        </div>
        <div>
            These are the data types currently aligned to this logical flow. You can toggle between a tabular and tree view of this information. Select a data type to see more information.
        </div>
        <br>
        <div>
            {#if activeMode === Modes.TREE}
                <DataTypeOverviewPanel primaryEntityReference={primaryEntityRef}/>
            {:else if activeMode === Modes.TABLE}
                <DataTypeDecoratorViewGrid/>
            {/if}
        </div>
    </div>
    {#if $selectedDecorator || $selectedDataType}
        <div class="decorator-context-panel">
            <DataTypeDetailContextPanel/>
        </div>
    {/if}
</div>


<style>

    .decorator-section {
        display: flex;
        gap: 10px;
    }

    .decorator-table {
        flex: 1 1 50%
    }

    .decorator-context-panel {
        width: 30%;
        padding-left: 1em;
        word-wrap: anywhere;
    }

</style>
