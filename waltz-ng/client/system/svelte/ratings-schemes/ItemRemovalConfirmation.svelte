<script>
    export let item;
    export let doCancel;
    export let doRemove;

    function onRemove() {
        doRemove(item.id);
    }
</script>

<div class="removal-box removal-warning">
    <h3>Confirm rating item removal</h3>
    Are you sure you want to remove this assessment definition ?

    <div>
        <h4>{item.name}</h4>
        <p>{item.description}</p>
    </div>

    <button class="btn btn-danger"
            title="Remove rating item"
            on:click={onRemove}>
        Remove
    </button>
    <button class="btn btn-link"
            on:click={() => doCancel()}>
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

