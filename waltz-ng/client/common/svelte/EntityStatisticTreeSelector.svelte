<script>
    import {buildHierarchies, doSearch, prepareSearchNodes} from "../hierarchy-utils";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";
    import {entityStatisticStore} from "../../svelte-stores/entity-statistic-store";
    import StatisticTreeNode from "./StatisticTreeNode.svelte";

    export let multiSelect = false;
    export let selectionFilter = () => true;

    const root = {name: "Root"};


    function calcDisplayHierarchy(nodes, query) {
        const searchResult = _.map(
            doSearch(query, nodes),
            d => Object.assign(
                {},
                d,
                {isExpanded: !_.isEmpty(query)}
            ));

        return buildHierarchies(searchResult, false);
    }

    let statisticsCall = entityStatisticStore.findAllActiveDefinitions();
    let expanded = true;
    let qry = "";
    let searchNodes = [];
    let statistics = [];

    $: statistics = $statisticsCall.data;
    $: searchNodes = prepareSearchNodes(statistics);
    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);

</script>

<SearchInput bind:value={qry}/>

<div class="waltz-scroll-region-250">
    <StatisticTreeNode {multiSelect}
                       {selectionFilter}
                       isRoot={true}
                       node={root}
                       childNodes={displayedHierarchy}
                       expanded={expanded}
                       on:select/>

</div>

