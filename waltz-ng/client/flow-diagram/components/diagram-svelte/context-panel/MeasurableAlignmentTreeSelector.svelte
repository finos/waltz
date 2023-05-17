<script>
    import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../../common/hierarchy-utils";
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import _ from "lodash";
    import MeasurableAlignmentTreeNode from "./MeasurableAlignmentTreeNode.svelte";
    import {createEventDispatcher} from "svelte";

    const root = {name: "Root"};

    export let alignments;
    const dispatch = createEventDispatcher();


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

    $: measurables = _.map(alignments, d => d.measurable);
    $: applicationsByMeasurableId = _.reduce(
        alignments,
        (acc, d) => {
            acc[d.measurable.id] = d.applicationIds;
            return acc;
        },
        {});


    let expanded = true;
    let qry = "";
    let searchNodes = [];
    let measurables = [];

    $: searchNodes = prepareSearchNodes(measurables);
    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);



    function selectNode(e) {
        dispatch("select", e.detail);
    }

</script>

<SearchInput bind:value={qry}/>

<div class="waltz-scroll-region-250">
    <MeasurableAlignmentTreeNode isRoot={true}
                                 node={root}
                                 childNodes={displayedHierarchy}
                                 expanded={expanded}
                                 on:select={selectNode}
                                 {applicationsByMeasurableId}>
    </MeasurableAlignmentTreeNode>
</div>

