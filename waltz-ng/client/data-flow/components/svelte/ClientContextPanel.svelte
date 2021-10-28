<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {contextPanelMode, selectedClient} from "./flow-decorator-store";
    import PhysicalFlowDetailTable from "./PhysicalFlowDetailTable.svelte";
    import {createEventDispatcher} from "svelte";
    import DataTypeDetailTable from "./DataTypeDetailTable.svelte";
    import EntityInfoPanel from "../../../common/svelte/info-panels/EntityInfoPanel.svelte";

    export let parentEntity;
    export let flowInfo;

    let selectedTab = "context";
    let dispatch = createEventDispatcher();

    function focusOnEntity(selectedEntity) {
        dispatch('selectEntity', selectedEntity);
    }

    function focusOnDataType(decorator) {
        dispatch('selectDecorator', decorator);
    }

    function cancel() {
        $selectedClient = null;
        $contextPanelMode = Modes.DEFAULT
    }

</script>

<EntityInfoPanel primaryEntityRef={$selectedClient}>

    <div slot="post-header">
        <div style="margin-bottom: 1em;">
            <DataTypeDetailTable on:selectDecorator={focusOnDataType}
                                 {flowInfo}
                                 {parentEntity}/>
            <PhysicalFlowDetailTable {flowInfo} {parentEntity}/>
        </div>
    </div>

</EntityInfoPanel>

<br>

<div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
    <ul>
        <li>
            <button class="btn btn-skinny" on:click={() => focusOnEntity($selectedClient)}>
                <Icon name="dot-circle-o"/>Focus diagram on {$selectedClient.name}
            </button>
        </li>
        <li>
            <button class="btn btn-skinny"
                     on:click={() => cancel()}>
                <Icon name="times"/>Cancel
            </button>
        </li>
    </ul>
</div>


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