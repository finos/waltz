<script>
    import { filters } from "./filter-store";
    import Pill from "../../../../common/svelte/Pill.svelte";
    import {onMount} from "svelte";

    export let pillDefs = {};
    export let deffCounts = {};

    onMount(() => {
        Object.keys(pillDefs)
            .map(key => {
                const count = deffCounts[key] ?? 0;
                pillDefs[key].name += ` (${count})`;
            });
    })

    const updateFilters = (filterKey) => {
        const selected = $filters.state.includes(filterKey);
        if (selected) {
            $filters.state = $filters.state.filter(f => f !== filterKey);
        } else {
            $filters.state = [...$filters.state, filterKey];
        }
    };

    // divs with on click must be accompanied by other keydown/keypress events
    const handleKeyDown = (event, key) => {
        if(event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            updateFilters(key);
        }
    };
</script>

<div style="margin: 0rem 0rem 0.5rem 0rem">
    <span class="filter-pills">
    Filters:
    {#each Object.keys(pillDefs) as key}
        <div
            role="button"
            tabindex="0"
            on:click={() => updateFilters(key)}
            on:keydown={(e) => handleKeyDown(e, key)}>
            <Pill pillDefs={pillDefs}
                pillKey={key}
                cleanPill={!$filters.state.includes(key)}
            />
        </div>
    {/each}
    </span>
</div>

<style>
.filter-pills {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    flex-wrap: wrap;
    max-width: 100%;
}
</style>