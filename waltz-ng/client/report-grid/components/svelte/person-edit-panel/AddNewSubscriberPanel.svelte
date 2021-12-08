<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {personStore} from "../../../../svelte-stores/person-store";

    export let onCancel = () => console.log("Cancelling");
    export let onSelect = () => console.log("Selecting");

    let selectedPerson = null;

    $: personCall = selectedPerson && personStore.getById(selectedPerson?.id)
    $: person = $personCall?.data;

    function selectPerson(e){
        selectedPerson = e.detail;
    }

</script>

<h5><Icon name="plus"/>Adding a subscriber:</h5>
<EntitySearchSelector entityKinds={['PERSON']}
                      on:select={selectPerson}/>
<p class="text-muted small">
    <Icon name="info-circle"/>
    Search for a person to add as a subscriber to this grid. Once subscribed a user can be promoted to owner
</p>

{#if selectedPerson}
    <button class="btn btn-success"
            on:click={() => onSelect(person)}>
        <Icon name="save"/>Add
    </button>
{/if}
<button class="btn btn-skinny"
        on:click={onCancel}>
    <Icon name="times"/>Cancel
</button>
