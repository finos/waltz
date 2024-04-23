<script>
    import {entitySearchStore} from "../../svelte-stores/entity-search-store";
    import AutoComplete from "simple-svelte-autocomplete";
    import {createEventDispatcher} from "svelte";
    import _ from "lodash";

    export let entityKinds;
    export let placeholder = "Search...";
    export let showClear = true;
    export let selectionFilter = () => true;

    const dispatch = createEventDispatcher();

    async function search(qry){
        const response = await entitySearchStore.search(qry, entityKinds);
        return _.filter(response.data, selectionFilter);
    }

    let selectedItem = null;

    $: dispatch("select", selectedItem);

</script>


<AutoComplete searchFunction={search}
              delay="300"
              labelFieldName="name"
              valueFieldName="id"
              cleanUserText={false}
              localFiltering={false}
              {placeholder}
              {showClear}
              className="waltz-search-input"
              bind:selectedItem={selectedItem} />
