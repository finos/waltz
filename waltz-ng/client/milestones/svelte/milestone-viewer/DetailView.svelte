<script>
    import {dynamicDate, fixedDate} from "./stores/selected-dates";
    import PointInTimeDetailView from "./PointInTimeDetailView.svelte";
    import DiffDetailView from "./DiffDetailView.svelte";

    export let data;
</script>


{#if ($fixedDate && $dynamicDate)}
    <DiffDetailView {data}/>
{:else if $dynamicDate}
    <PointInTimeDetailView {data}/>
{:else if $fixedDate}
    <div class="alert alert-info">
        Now pick a comparison point
    </div>
{:else}
    <h2>Instructions</h2>
    <p>
        blah de blah de blah
        blah de blah de blah
        blah de blah de blah
        blah de blah de blah
        blah de blah de blah
        blah de blah de blah
        blah de blah de blah
        blah de blah de blah
    </p>
{/if}

