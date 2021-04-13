<script>
    import {entitySearchStore} from "../../svelte-stores/entity-search-store";
    import AutoComplete from "simple-svelte-autocomplete";
    import {createEventDispatcher} from "svelte";

    let selectedItem;
    export let entityKinds;
    export let placeholder = "Search..."

    const dispatch = createEventDispatcher();

    async function search(qry){
        const response = await entitySearchStore.search(qry, entityKinds);
        return response.data;
    }

    $: dispatch("select", selectedItem);

</script>


<AutoComplete searchFunction={search}
              labelFieldName="name"
              valueFieldName="id"
              {placeholder}
              bind:selectedItem={selectedItem} />
