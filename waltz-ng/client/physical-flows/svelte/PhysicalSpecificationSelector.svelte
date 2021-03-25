<script>

    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../common";

    import {physicalSpecification} from "./physical-flow-editor-store";

    export let specifications = [];

    let filteredSpecs = [];
    let qry = "";

    $: filteredSpecs = _.isEmpty(qry)
            ? specifications
            : termSearch(specifications, qry, ["name", "externalId", "format"]);

</script>


<div class="small">

    <SearchInput bind:value={qry}/>

    <table class="table table-condensed table-hover">
        <thead>
        <th>Name</th>
        <th>External Id</th>
        <th>Format</th>
        </thead>
        <tbody>
        {#each filteredSpecs as spec}
            <tr class="clickable"
                on:click={() => $physicalSpecification = spec}>
                <td>{spec.name}</td>
                <td>{spec.externalId}</td>
                <td>{spec.format}</td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style type="text/scss">
</style>