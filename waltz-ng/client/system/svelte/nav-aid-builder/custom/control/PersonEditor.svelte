<script>
    import { createEventDispatcher } from 'svelte';
    import EntitySearchSelector from "../../../../../common/svelte/EntitySearchSelector.svelte";

    export let params;

    const dispatch = createEventDispatcher();

    console.log("Here we are")

    $: console.log({params})
</script>


<h3>{params.message}</h3>

<label for="title">
    Title
</label>
<input id="title"
       type="text"
       bind:value={params.data.title}
       placeholder="Title"/>

<!--<label for="leaderPerson">-->
<!--    Person-->
<!--</label>-->

<!--<span id="leaderPerson">-->
<!--    <EntitySearchSelector entityKinds={["PERSON"]}-->
<!--                          on:select={evt => miniModel.person = evt.detail}/>-->
<!--</span>-->

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
