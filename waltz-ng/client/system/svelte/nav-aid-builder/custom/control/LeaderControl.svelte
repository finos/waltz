<script>
    import { createEventDispatcher } from 'svelte';
    import EntitySearchSelector from "../../../../../common/svelte/EntitySearchSelector.svelte";

    const dispatch = createEventDispatcher();

    let miniModel = {
        title: null,
        person: null
    };

    $: dispatch("update", miniModel);
</script>

<label for="title">
    Title
</label>
<input id="title"
       type="text"
       bind:value={miniModel.title}
       placeholder="Title"/>

<label for="leaderPerson">
    Person
</label>

<span id="leaderPerson">
    <EntitySearchSelector entityKinds={["PERSON"]}
                          on:select={evt => miniModel.person = evt.detail}/>
</span>
