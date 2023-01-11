<script>
    export let assessment;
    export let selectedRating;
    export let doRemove;
    export let onCancel;

    $: console.log({assessment, selectedRating});

</script>

<div class="removal-warning">
    <h3>Confirm assessment rating removal</h3>
    Are you sure you want to remove this assessment ?

    <div>
        <b>{assessment.definition.name}</b>
        <p>{selectedRating.name}</p>
    </div>

    <button class="btn btn-warning"
            on:click={doRemove}>
        Remove
    </button>
    <button class="btn btn-link"
            on:click={onCancel}>
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