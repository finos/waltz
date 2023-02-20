<script>
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import TaxonomyNavAidBuilder from "./TaxonomyNavAidBuilder.svelte";

    let dtCall = dataTypeStore.findAll();
    let taxonomy = [];

    $: taxonomy = $dtCall.data;
</script>


<TaxonomyNavAidBuilder taxonomy={taxonomy}
                       link='data-types'/>
