<script>
    import {contextPanelMode, Modes} from "./flow-decorator-store";
    import {createEventDispatcher, onMount} from "svelte";
    import FlowDecoratorGraphFilters from "./FlowDecoratorGraphFilters.svelte";
    import ToastStore from "../../../svelte-stores/toast-store";
    import DefaultContextPanel from "./DefaultContextPanel.svelte";
    import ClientContextPanel from "./ClientContextPanel.svelte";
    import FlowDecoratorContextPanel from "./FlowDecoratorContextPanel.svelte";

    export let parentEntity;
    export let flowInfo;

    let selectedTab = "context";
    let dispatch = createEventDispatcher();

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
            {#if $contextPanelMode === Modes.DEFAULT}
                <DefaultContextPanel {parentEntity}/>
            {/if}

            {#if $contextPanelMode === Modes.ENTITY}
                <ClientContextPanel {parentEntity}
                                    {flowInfo}
                                    on:selectEntity={focusOnEntity}/>
            {/if}

            {#if $contextPanelMode === Modes.DECORATOR}
                <FlowDecoratorContextPanel {parentEntity}
                                      {flowInfo}/>
            {/if}

        {:else if selectedTab === 'filters'}
            <FlowDecoratorGraphFilters/>
        {/if}
    </div>
</div>
