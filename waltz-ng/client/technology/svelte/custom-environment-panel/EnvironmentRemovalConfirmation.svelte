<script>
    export let environment;
    export let doRemove;
    export let doCancel;
</script>

<div class="removal-warning">
    <h3>Confirm environment removal</h3>
    Are you sure you want to remove this environment and its mappings?

    <div>
        <b>{environment.groupName} / {environment.name}</b>
        <p>{environment.description || "No description provided"}</p>
    </div>

    <button class="btn btn-warning"
            on:click={() => doRemove(environment)}>
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