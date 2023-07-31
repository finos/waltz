<script>

    import DiagramTreeNode from "./DiagramTreeNode.svelte";
    import _ from "lodash";
    import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../../common/hierarchy-utils";
    import {sameRef} from "../../../../common/entity-utils";
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";

    export let groups;
    export let depth = 0;
    export let selected = [];
    export let onSelect = () => console.log("selecting")
    export let onDeselect = () => console.log("deselecting")

    let expanded = true;
    let qry = "";
    let searchNodes = [];

    function calcDisplayHierarchy(nodes, query, selected) {

        const searchResult = _.map(
            doSearch(query, nodes),
            d => Object.assign(
                {},
                d,
                {
                    isExpanded: !_.isEmpty(query),
                    isSelected: _.some(selected, r => sameRef(r, d))
                }
            ));

        return {name:"root", hideNode: true, children: buildHierarchies(searchResult, false)}
    }

    $: searchNodes = prepareSearchNodes(groups, "title");
    $: tree = calcDisplayHierarchy(searchNodes, qry, selected);

</script>

<SearchInput bind:value={qry}/>
<div style="margin-top: 0.5em;">
    <DiagramTreeNode {tree}
                     {depth}
                     {expanded}
                     {onSelect}
                     {onDeselect}/>
</div>
