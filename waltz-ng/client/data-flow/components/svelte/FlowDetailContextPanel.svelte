<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {arcs, clearSelections, selectedClient} from "./flow-decorator-store";
    import PhysicalFlowDetailTable from "./PhysicalFlowDetailTable.svelte";
    import {createEventDispatcher} from "svelte";
    import DataTypeDetailTable from "./DataTypeDetailTable.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";


    export let parentEntity;

    let selectedTab = "context";
    let dispatch = createEventDispatcher();

    function focusOnEntity(selectedEntity) {
        dispatch('selectEntity', selectedEntity);
    }

    function focusOnDataType(decorator) {
        dispatch('selectDecorator', decorator);
    }

    function cancel() {
        clearSelections();
    }

    $: logicalFlowId = _
        .chain($arcs)
        .filter(a => a.clientId === $selectedClient?.id)
        .map(a => a.flowId)
        .first()
        .value();

</script>


{#if logicalFlowId}
    <div style="margin-bottom: 1em;">
        <h4>Flow Detail for:
            <EntityLink ref={$selectedClient}/>
        </h4>
        <div class="help-block small">
            <Icon name="info-circle"/>
            Select a data type in the list below to filter the physical flows which share that data type.
        </div>
        <DataTypeDetailTable on:selectDecorator={focusOnDataType}
                             {logicalFlowId}/>
        <br>
        <PhysicalFlowDetailTable {parentEntity}
                                 {logicalFlowId}/>
    </div>

    <br>

    <div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
        <ul>
            <li>
                <button class="btn btn-skinny" on:click={() => focusOnEntity($selectedClient)}>
                    <Icon name="dot-circle-o"/>
                    Focus diagram on {$selectedClient?.name}
                </button>
            </li>
            <li>
                <button class="btn btn-skinny"
                        on:click={() => cancel()}>
                    <Icon name="times"/>
                    Cancel
                </button>
            </li>
        </ul>
    </div>
{/if}


<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>