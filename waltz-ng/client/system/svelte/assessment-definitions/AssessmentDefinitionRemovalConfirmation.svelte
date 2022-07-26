<script>
    import Icon from "../../../common/svelte/Icon.svelte";

    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating";
    import {selectedDefinition} from "./assessment-definition-utils";

    export let doCancel;
    export let doRemove;

    let ratingCall, ratings;
    let canRemove = false;
    let removePromise = null;

    function onRemove() {
        removePromise = doRemove($selectedDefinition.id);
    }

    $: ratingCall = assessmentRatingStore.findByDefinitionId($selectedDefinition.id);
    $: ratings = $ratingCall.data;
    $: canRemove = ratings.length === 0;

</script>

{#if $ratingCall.status === 'loading'}
    <h4 class="text-muted">
        Analyzing impact of removing {$selectedDefinition.name}...
    </h4>
{:else}
    <div class="removal-box"
         class:removal-warning={ratings.length === 0}
         class:removal-error={ratings.length > 0}>
        <h3>Confirm assessment definition removal</h3>
        Are you sure you want to remove this assessment definition ?

        <div>
            <h4>{$selectedDefinition.name}</h4>
            <p>{$selectedDefinition.description}</p>
        </div>

        {#if ratings.length > 0}
            <div class="reconsider">
                There are {ratings.length} ratings associated with this definition.
                <br>
                If you proceed these will be lost.
                <br>
                Please ensure you have a backup of the data before proceeding.
                <br>
                You can obtain a backup from the export on the
                <a href="../../assessment-definition/{$selectedDefinition.id}">overview page</a>.
                <br>
                <br>
                <button class="btn btn-danger"
                        title="Confirm removal"
                        on:click={() => canRemove = true}>
                    I understand
                </button>
                {#if canRemove}
                    <p style="margin-top: 0.5em">
                        Removal button (below) is now enabled
                    </p>
                {/if}

            </div>
        {/if}

        <button class="btn"
                class:btn-warning={ratings.length === 0}
                class:btn-danger={ratings.length > 0}
                disabled={!canRemove}
                title="Remove definition"
                on:click={onRemove}>
            Remove
        </button>
        <button class="btn btn-link"
                on:click={() => doCancel()}>
            Cancel
        </button>


        {#if removePromise}
            {#await removePromise}
                Removing...
            {:then r}
                Removed!
            {:catch e}
            <span class="alert alert-warning">
                Failed to remove assessment definition. Reason: {e.error}
                <button class="btn-link"
                        on:click={() => removePromise = null}>
                    <Icon name="check"/>
                    Okay
                </button>
            </span>
            {/await}
        {/if}
    </div>
{/if}

<style>
    .removal-box{
        border-width: 1px;
        border-style: solid;
        background-color: #faf8e9;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        border-radius: 2px;

    }
    .removal-warning {
        border-color: #D9923F;
        background-color: #faf8e9;
    }
    .removal-error {
        border-color: #d93f44;
        background-color: #fae9ee;
    }

    .reconsider {
        border: 2px solid black;
        padding: 1em;
        margin-bottom: 1em;
        margin-top: 0.5em;
        font-weight: bolder;
        background-color: #fd9090;
    }
</style>

