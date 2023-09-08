<script>

    import {createEventDispatcher} from "svelte";

    export let diagram;

    const dispatch = createEventDispatcher();

    function remove() {
        dispatch("remove", diagram);
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


<h4>Are you sure you want to remove diagram: {diagram.name}?</h4>
<div class="help-block">This will remove it from all views for all users.
    If you wish to retain the diagram but hide from view you should update the group information to a reference that is not used on a page.
    e.g. '<i>GROUP_NAME</i>.OLD'
</div>

<div>
    <button class="btn btn-danger"
            on:click={remove}>
        Remove
    </button>
    <button class="btn btn-default"
            on:click={cancel}>
        Cancel
    </button>
</div>