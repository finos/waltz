<script>

    import EntityLabel from "../../common/svelte/EntityLabel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";

    import {sameRef} from "../../common/entity-utils";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../common";
    import _ from "lodash";

    import {createEventDispatcher} from "svelte";

    export let flows = [];
    export let node = null;

    let filteredFlows = [];
    let qry = "";

    let dispatch = createEventDispatcher();

    function selectFlow(flow) {
        dispatch("select", flow)
    }

    $: filteredFlows = _.isEmpty(qry)
        ? flows
        : termSearch(flows, qry, ["source.name", "source.externalId", "target.name", "target.externalId"]);

</script>


<div class="small">

    <SearchInput bind:value={qry}/>

    <div class:waltz-scroll-region-300={filteredFlows.length > 10}>
        <table class="table table-condensed table-hover small">
            <colgroup>
                <col width="48%">
                <col width="4%">
                <col width="48%">
            </colgroup>
            <thead>
            <tr>
                <th>Source</th>
                <th/>
                <th>Target</th>
            </tr>
            </thead>
            <tbody>
            {#each filteredFlows as flow}
                <tr class="clickable"
                    on:click={() => selectFlow(flow)}>
                    <td class:counterpart={node && !sameRef(node, flow.source)}
                        class:mainNode={node && sameRef(node, flow.source)}>
                        <EntityLabel ref={flow.source}/>
                    </td>
                    <td class="counterpart">
                        <Icon name="arrow-right"/>
                    </td>
                    <td class:counterpart={node && !sameRef(node, flow.target)}
                        class:mainNode={node && sameRef(node, flow.target)}>
                        <EntityLabel ref={flow.target}/>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>

</div>


<style type="text/scss">
    @import "../../../style/_variables.scss";

    .mainNode {
        color: #999;
    }

    tr:hover .counterpart {
        background-color: $waltz-green-background;
    }
</style>