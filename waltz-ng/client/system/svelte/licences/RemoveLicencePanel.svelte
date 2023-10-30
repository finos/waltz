<script>

    import {createEventDispatcher} from "svelte";

    export let licence;

    const dispatch = createEventDispatcher();

    function remove() {
        dispatch("remove", licence);
    }

    function cancel() {
        dispatch("cancel");
    }

</script>

<div>
    <h4>Are you sure you want to remove licence: <strong>{licence.name}</strong> <span class="text-muted small">{licence.externalId}</span></h4>
    {#if licence.description}
        <div class="help-block">{licence.description}</div>
    {/if}

    <p style="padding-top: 1em">This will permanently remove this licence.</p>

    <div style="padding-top: 1em">
        <button class="btn btn-danger"
                on:click={remove}>
            Remove
        </button>
        <button class="btn btn-default"
                on:click={cancel}>
            Cancel
        </button>
    </div>
</div>
