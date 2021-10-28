<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {contextPanelMode, Modes, selectedDecorator} from "./flow-decorator-store";
    import EntityInfoPanel from "../../../common/svelte/info-panels/EntityInfoPanel.svelte";
    import FlowDatatypeDetailTable from "./FlowDatatypeDetailTable.svelte";

    function cancel() {
        $contextPanelMode = Modes.DEFAULT
        $selectedDecorator = null;
    }

</script>

<EntityInfoPanel primaryEntityRef={$selectedDecorator.entityReference}>

    <div slot="post-header">
        <div style="margin-bottom: 1em;">

            <FlowDatatypeDetailTable />

        </div>
    </div>

</EntityInfoPanel>

<br>

<div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
    <ul>
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