<script>
    import FilterSelectorPanel from "./FilterSelectorPanel.svelte";
    import FilterList from "./FilterList.svelte";
    import {getContext} from "svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    const filterParameters = getContext("filterParameters");

    const Modes = {
        LIST: "LIST",
        SELECTOR: "SELECTOR"
    }

    let activeMode = Modes.LIST;

</script>

<h4>
    <Icon name="filter"/>
    Filters
</h4>
<p class="help-block">
    Assessments can be used to further filter the entities used to drive this overlay diagram.
</p>
<p class="help-block">
    Filters can be combined, if multiple are selected entities must match all the filters.
</p>

{#if activeMode === Modes.LIST}
    <FilterList on:edit={() => activeMode = Modes.SELECTOR}/>
{:else if activeMode === Modes.SELECTOR}
    <FilterSelectorPanel on:close={() => activeMode = Modes.LIST}/>
{/if}