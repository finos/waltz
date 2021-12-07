<script>

    export let column;
    export let onCancel = () => console.log("Cancelling");
    export let onRemove = () => console.log("Removing");

</script>


<p>
    Are you sure you want to remove <strong>{column?.columnEntityReference?.name}</strong> from this report grid?
</p>

<button class="btn btn-danger" on:click={() => onRemove(column)}>Remove</button>
<button class="btn btn-default" on:click={() => onCancel()}>Cancel</button>