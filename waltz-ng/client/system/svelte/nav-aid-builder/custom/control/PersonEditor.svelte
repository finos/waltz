<script>
    import { createEventDispatcher } from 'svelte';
    import EntitySearchSelector from "../../../../../common/svelte/EntitySearchSelector.svelte";

    export let params;

    const dispatch = createEventDispatcher();

</script>


<h3>{params.message}</h3>

<label for="title">
    Title
</label>
<input id="title"
       type="text"
       bind:value={params.data.title}
       placeholder="Title"/>
<div class="help-block small">
    The title of the person, e.g. Chief Information Officer
</div>



<label style="padding-top: 1.2em;"
       for="leaderPerson">
    Person
</label>

<div id="leaderPerson">
    <EntitySearchSelector entityKinds={["PERSON"]}
                          on:select={evt => params.data.person = evt.detail}/>
</div>
<div class="help-block small">
    The person holding this title
</div>

<hr>

<button class="btn btn-primary"
        on:click={() => dispatch("confirm", params)}>
    Save
</button>
<button class="btn-skinny"
        on:click={() => dispatch("cancel")}>
    Cancel
</button>


<style>
    #title {
        width: 100%;
    }
</style>
