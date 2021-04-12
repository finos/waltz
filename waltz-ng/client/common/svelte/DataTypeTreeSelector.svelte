<script>
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {buildHierarchies, doSearch, prepareSearchNodes} from "../hierarchy-utils";
    import DataTypeTreeNode from "./DataTypeTreeNode.svelte";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";

    let dataTypesCall = dataTypeStore.findAll();
    let expanded = true;
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
    $: searchNodes = prepareSearchNodes(dataTypes);
    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);


</script>
<SearchInput bind:value={qry}></SearchInput>
<div class="waltz-scroll-region-250">
    <DataTypeTreeNode isRoot={true}
                      node={{name: "Root"}}
                      childNodes={displayedHierarchy}
                      expanded={expanded}
                      on:select>
    </DataTypeTreeNode>

</div>
<!--<pre>{JSON.stringify(displayedHierarchy, null, 2)}</pre>-->

