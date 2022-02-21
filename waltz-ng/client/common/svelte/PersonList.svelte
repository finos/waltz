<script>
    import Icon from "./Icon.svelte";
    import EntityLabel from "./EntityLabel.svelte";
    import EntitySearchSelector from "./EntitySearchSelector.svelte";
    import {personStore} from "../../svelte-stores/person-store";
    import {mayRemove} from "./person-list-utils";
    import _ from "lodash";

    const Modes = {
        ADD: "add",
        LIST: "list"
    };

    const searchKinds = ["PERSON"];

    export let people = [];
    export let canAdd = false;
    export let canRemove = false;
    export let canRemoveSelf = true;

    export let onAdd = (p) => console.log("Default onAdd", p) || Promise.resolve();
    export let onRemove = (p) => console.log("Default onRemove", p) || Promise.resolve();

    let mode = Modes.LIST;


    function initiateAddition() {
        mode = Modes.ADD;
    }

    function doAdd(evt) {
        const person = evt.detail;
        return onAdd(person)
            .then(mode = Modes.LIST);
    }

    $: selfCall = personStore.getSelf();
    $: self = $selfCall.data;

    $: peopleList = _.map(
        people,
        p => ({
            person: p,
            isRemovable: mayRemove(p, self, {canRemove, canRemoveSelf})
        }));

    $: peopleIds = _.map(people, d => d.id);

    function notAlreadyPicked(d) {
        return !_.includes(peopleIds, d.id);
    }

</script>

{#if mode === Modes.LIST}
    <!-- LIST -->
    <ul class="list-unstyled">
        {#each peopleList as p}
            <!-- EXISTING PEOPLE, note: the  -->
            <li class:removed={p.person.isRemoved }
                class="waltz-visibility-parent">
                {#if p.isRemovable}
                    <!-- remove btn -->
                    <button class="btn-skinny remove waltz-visibility-child-10"
                            on:click={() => onRemove(p.person)}
                            title="Remove person">
                        <Icon name="minus-circle" fixedWidth={true}/>
                    </button>
                {/if}
                {#if ! p.isRemovable && canRemove}
                    <!-- spacer -->
                    <button class="btn-skinny remove"
                            disabled>
                        <Icon name="fw"/>
                    </button>
                {/if}
                <EntityLabel ref={p.person}
                             showIcon={false}/>
            </li>
        {/each}
        {#if canAdd}
            <!-- ADD PERSON -->
            <li class="waltz-visibility-parent">
                <button class="btn-skinny add waltz-visibility-child-50"
                        on:click={initiateAddition}
                        title="Add an additional person">
                    <Icon name="plus-circle"/>
                    Add additional person
                </button>
            </li>
        {/if}

    </ul>

{:else if mode === Modes.ADD}

    <!-- ADD -->
    <strong>Add additional person</strong>

    <EntitySearchSelector entityKinds={searchKinds}
                          on:select={doAdd}
                          selectionFilter={notAlreadyPicked}/>
    <button class="btn-link"
            on:click={() => mode = Modes.LIST}>
        Cancel
    </button>

{/if}


<style>
    .remove {
        color: red;
        padding-left: 0.5em; /* increase size of 'hitbox' */
    }

    .add {
        color: green;
        padding-left: 0.5em; /* keep in-sync with the removal hitbox */
    }
</style>