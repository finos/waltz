<script>

    import FilterSelectorPanel from "./FilterSelectorPanel.svelte";
    import FilterList from "./FilterList.svelte";
    import {getContext} from "svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let existingFilters = false;

    const filterParameters = getContext("filterParameters");

    const Modes = {
        LIST: "LIST",
        SELECTOR: "SELECTOR"
    }

    let activeMode = existingFilters
        ? Modes.LIST
        : Modes.SELECTOR;

</script>

<h4>
    <Icon name="filter"/>
    Filters
</h4>
{#if activeMode === Modes.LIST}
    <FilterList on:edit={() => activeMode = Modes.SELECTOR}/>
{:else if activeMode === Modes.SELECTOR}
    <FilterSelectorPanel on:cancel={() => activeMode = Modes.LIST}/>
{/if}