<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {flowDirection, flowDirections, layoutDirection, layoutDirections, selectedClient} from "./flow-decorator-store";
    import Toggle from "../../../common/svelte/Toggle.svelte";
    import _ from "lodash";
    import PhysicalFlowDetailTable from "./PhysicalFlowDetailTable.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import FlowDecoratorGraphFilters from "./FlowDecoratorGraphFilters.svelte";
    import DataTypeDetailTable from "./DataTypeDetailTable.svelte";
    import ToastStore from "../../../svelte-stores/toast-store";

    export let parentEntity;
    export let flowInfo;

    let selectedTab = "context";
    let dispatch = createEventDispatcher();

    function toggleDirection() {
        $layoutDirection = $layoutDirection === layoutDirections.categoryToClient
            ? layoutDirections.clientToCategory
            : layoutDirections.categoryToClient
    }

    function focusOnEntity(selectedEntity) {
        dispatch('select', selectedEntity);
    }

    onMount(() => ToastStore.info("This is a beta view, we'd love to hear feedback!"));

</script>


<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="context"
           id="context">
    <label class="wt-label"
           for="context">
        <span>Context</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="filters"
           id="filters">
    <label class="wt-label"
           for="filters">
        <span>Filters</span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'context'}

            {#if !$selectedClient}
            <div class="help-block">
                <Icon name="info-circle"/>
                Select a datatype or entity on the diagram for further information. Click the plus icon
                to drill down to child data types. Filters in the next tab can be used to simplify the view.
            </div>
            <div>
                You are currently viewing
                <strong>{_.toLower($flowDirection)}</strong>
                flows
                {$flowDirection === flowDirections.INBOUND ?  "to" : "from"}
                {parentEntity.name || "unknown entity" }
            </div>
            <br>
            <div>
                <Toggle labelOn="Show inbound flows"
                        labelOff="Show outbound flows"
                        state={$layoutDirection === layoutDirections.categoryToClient}
                        onToggle={() => toggleDirection()}/>
            </div>
            {/if}

            {#if $selectedClient}
                <h4 style="padding-bottom: 1em">
                    <EntityLink ref={$selectedClient}/>
                </h4>

                <div>
                    <DataTypeDetailTable {flowInfo} {parentEntity}/>
                </div>

                <br>

                <div>
                    <PhysicalFlowDetailTable {flowInfo} {parentEntity}/>
                </div>

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
                                     on:click={() => $selectedClient = null}>
                                <Icon name="times"/>Cancel
                            </button>
                        </li>
                    </ul>
                </div>
            {/if}

        {:else if selectedTab === 'filters'}
            <FlowDecoratorGraphFilters/>
        {/if}
    </div>
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