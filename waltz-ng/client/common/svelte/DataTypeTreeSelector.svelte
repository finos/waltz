<script>
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {buildHierarchies, doSearch, prepareSearchNodes, reduceToSelectedNodesOnly} from "../hierarchy-utils";
    import DataTypeTreeNode from "./DataTypeTreeNode.svelte";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";

    export let multiSelect = false;
    export let nonConcreteSelectable = true;
    export let selectionFilter = () => true;
    export let expanded = true;
    export let dataTypeIds = [];

    const root = {name: "Root", isExpanded: true};


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

    let dataTypesCall = dataTypeStore.findAll();
    let qry = "";
    let searchNodes = [];
    let dataTypes = [];

    $: dataTypes = $dataTypesCall.data;
    $: requiredNodes = _.isEmpty(dataTypeIds)
        ? dataTypes
        : reduceToSelectedNodesOnly(dataTypes, dataTypeIds);
    $: searchNodes = prepareSearchNodes(requiredNodes);
    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);

</script>

<SearchInput bind:value={qry}/>

<div class="waltz-scroll-region-350">
    <DataTypeTreeNode {multiSelect}
                      {selectionFilter}
                      {nonConcreteSelectable}
                      isRoot={true}
                      node={root}
                      childNodes={displayedHierarchy}
                      {expanded}
                      on:select/>
</div>

