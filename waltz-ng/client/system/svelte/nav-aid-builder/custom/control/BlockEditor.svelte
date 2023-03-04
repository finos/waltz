<script>
    import {createEventDispatcher} from "svelte";

    export let params;

    const dispatch = createEventDispatcher();

</script>

<h3>{params.message}</h3>

<label for="name">Name</label>
<input type="text"
       id="name"
       bind:value={params.data.name}/>

<hr>
<div>
    <button class="btn btn-primary"
            on:click={() => dispatch("confirm", params)}>
        Save
    </button>
    <button class="btn-skinny"
            on:click={() => dispatch("cancel")}>
        Cancel
    </button>
</div>

<style>
    input {
        width: 100%;
    }
</style>