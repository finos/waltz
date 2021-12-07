<script>
    import {entitySearchStore} from "../../svelte-stores/entity-search-store";
    import AutoComplete from "simple-svelte-autocomplete";
    import {createEventDispatcher} from "svelte";

    export let entityKinds;
    export let placeholder = "Search...";
    export let showClear = true;

    let selectedItem;

    const dispatch = createEventDispatcher();

    async function search(qry){
        const response = await entitySearchStore.search(qry, entityKinds);
        return response.data;
    }

    $: dispatch("select");

</script>


<AutoComplete searchFunction={search}
              labelFieldName="name"
              valueFieldName="id"
              {placeholder}
              {showClear}
              bind:selectedItem={selectedItem} />
