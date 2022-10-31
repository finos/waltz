<script>
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {buildHierarchies, doSearch, prepareSearchNodes} from "../hierarchy-utils";
    import DataTypeTreeNode from "./DataTypeTreeNode.svelte";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";

    export let multiSelect = false;
    export let nonConcreteSelectable = true;
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

    let dataTypesCall = dataTypeStore.findAll();
    let expanded = true;
    let qry = "";
    let searchNodes = [];
    let dataTypes = [];

    $: dataTypes = $dataTypesCall.data;
    $: searchNodes = prepareSearchNodes(dataTypes);
    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);

</script>

<SearchInput bind:value={qry}/>

<div class="waltz-scroll-region-250">
    <DataTypeTreeNode {multiSelect}
                      {selectionFilter}
                      {nonConcreteSelectable}
                      isRoot={true}
                      node={root}
                      childNodes={displayedHierarchy}
                      expanded={expanded}
                      on:select/>

</div>

