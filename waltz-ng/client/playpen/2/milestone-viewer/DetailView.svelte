<script>
    import {fixedDate, dynamicDate} from "./stores/selected-dates";
    import PointInTimeDetailView from "./PointInTimeDetailView.svelte";
    import DiffDetailView from "./DiffDetailView.svelte";

    export let data;
    export let measurablesById;
</script>

<h3>Detail</h3>

{#if ($fixedDate && $dynamicDate)}
    <DiffDetailView {data}
                    {measurablesById} />
{:else if $dynamicDate}
    <PointInTimeDetailView {data}
                           {measurablesById}/>
{:else if $fixedDate}
    <div class="alert alert-info">
        Now pick a comparison point
    </div>
{:else}
    Instructions
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

