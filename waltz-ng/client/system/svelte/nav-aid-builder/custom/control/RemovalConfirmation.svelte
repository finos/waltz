<script>
    import {createEventDispatcher} from "svelte";

    export let params;

    const dispatch = createEventDispatcher();

</script>

<div class="removal-box removal-warning">
    <h3>Confirm item removal</h3>
    Are you sure you want to remove this item ?

    <div>
        <h4>{params.message}</h4>
    </div>

    <button class="btn btn-danger"
            title="Remove item"
            on:click={() => dispatch("confirm", params)}>
        Remove
    </button>
    <button class="btn btn-link"
            on:click={() => dispatch("cancel")}>
        Cancel
    </button>

</div>

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
</style>

