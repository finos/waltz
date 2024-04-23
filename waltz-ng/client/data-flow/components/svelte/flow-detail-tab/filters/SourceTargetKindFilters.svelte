<script>

    import {FlowNodeTypes} from "../flow-detail-utils";
    import {mkNodeTypeFilter, mkNodeTypeFilterId} from "./filter-utils";
    import _ from "lodash";
    import {filters, removeFilter, updateFilters} from "../flow-details-store";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";

    const filterId = mkNodeTypeFilterId();
    const allTypes = _.map(FlowNodeTypes, d => d.key);

    function selectAll() {
        removeFilter(filterId);
    }

    function toggle(kind) {
        if (nodeTypeFilter == null) {
            updateFilters(
                filterId,
                mkNodeTypeFilter(filterId, _.without(allTypes, kind.key)));
        } else {
            const currentKinds = nodeTypeFilter.nodeTypes;
            const updatedKinds = _.includes(currentKinds, kind.key)
                ? _.without(currentKinds, kind.key)
                : _.concat(currentKinds, [kind.key]);

            updateFilters(
                filterId,
                mkNodeTypeFilter(
                    filterId,
                    updatedKinds));
        }

    }

    function deselectAll() {
        updateFilters(
            filterId,
            mkNodeTypeFilter(filterId, []));
    }

    $: nodeTypeFilter = _.find($filters, d => d.id === filterId);

    onMount(() => {
        selectAll();
    });

</script>

<div style="display: flex; flex-direction: column; padding-top: 1em;  padding-bottom: 1em">
    <div class="help-block">
        Use the buttons to filter on the types of either the flow source or target.
    </div>
    {#each FlowNodeTypes as kind}
        <label style="">
            <input type="checkbox"
                   name="nodeKinds"
                   checked={_.isNil(nodeTypeFilter) || _.includes(nodeTypeFilter?.nodeTypes, kind.key)}
                   on:change={() => toggle(kind)}
                   value={kind.key}/>
            <Icon name={kind.icon}/> {kind.name}
        </label>
    {/each}

    <div>
        <button class="btn-skinny" on:click={deselectAll}>Deselect All</button>
        |
        <button class="btn-skinny" on:click={selectAll}>Select All</button>
    </div>
</div>
