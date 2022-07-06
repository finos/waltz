<script>

    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../common";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import {nestedEnums} from "./physical-flow-editor-store";
    import {toDataFormatKindName} from "./physical-flow-registration-utils";

    export let specifications = [];

    let filteredSpecs = [];
    let qry = "";

    const dispatch = createEventDispatcher();

    $: filteredSpecs = _.isEmpty(qry)
        ? specifications
        : termSearch(specifications, qry, ["name", "externalId", "format"]);

    function selectSpec(spec) {
        dispatch("select", spec);
    }

</script>


<div class="small">
    <SearchInput bind:value={qry}/>
    <br>
    <div class:waltz-scroll-region-350={_.size(filteredSpecs) > 10}>
        <table class="table table-condensed table-hover">
            <colgroup>
                <col width="40%"/>
                <col width="40%"/>
                <col width="20%"/>
            </colgroup>
            <thead>
            <tr>
                <th>Name</th>
                <th>External Id</th>
                <th>Format</th>
            </tr>
            </thead>
            <tbody>
            {#each filteredSpecs as spec}
                <tr class="clickable"
                    on:click={() => selectSpec(spec)}>
                    <td class="force-wrap">{spec.name}</td>
                    <td class="force-wrap">{spec.externalId}</td>
                    <td>{toDataFormatKindName($nestedEnums, spec.format)}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</div>


<style type="text/scss">
</style>