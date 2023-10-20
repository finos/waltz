<script>
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {buildHierarchies, doSearch, prepareSearchNodes, reduceToSelectedNodesOnly} from "../hierarchy-utils";
    import DataTypeTreeNode from "./DataTypeTreeNode.svelte";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";

    export let selectionFilter = () => true;
    export let dataTypeIds = [];
    export let expanded = true;

    const root = {name: "Root", isExpanded: true};

    let dataTypesCall = dataTypeStore.findAll();

    let qry = "";
    let searchNodes = [];
    let dataTypes = [];

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

    $: dataTypes = $dataTypesCall.data;

    $: requiredNodes = reduceToSelectedNodesOnly(dataTypes, dataTypeIds);
    $: searchNodes = prepareSearchNodes(requiredNodes);

    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);

</script>

<SearchInput bind:value={qry}/>

<div style="padding-top: 1em">
    <DataTypeTreeNode {selectionFilter}
                      isRoot={true}
                      node={root}
                      childNodes={displayedHierarchy}
                      {expanded}
                      nonConcreteSelectable={false}
                      on:select/>
</div>

