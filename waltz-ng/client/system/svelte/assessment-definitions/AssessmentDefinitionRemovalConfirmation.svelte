<script>
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating";

    export let definition;
    export let doCancel;
    export let doRemove;

    $: ratingCall = assessmentRatingStore.findByDefinitionId(definition.id);
    $: ratings = $ratingCall.data;

</script>

<div class="removal-warning">
    <h3>Confirm assessment definition removal</h3>
    Are you sure you want to remove this assessment definition ?

    <div>
        <h4>{definition.name}</h4>
        <p>{definition.description}</p>
    </div>

    {#if ratings.length > 0}
        Aargh!!!
    {/if}

    <button class="btn btn-warning"
            on:click={() => doRemove(definition.id)}>
        Remove
    </button>
    <button class="btn btn-link"
            on:click={() => doCancel()}>
        Cancel
    </button>
</div>


<style>
    .removal-warning {
        border: 1px solid #D9923F;
        background-color: #faf8e9;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        border-radius: 2px;
    }
</style>

