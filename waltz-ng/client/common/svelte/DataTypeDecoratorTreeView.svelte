<script>
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {buildHierarchies, doSearch, prepareSearchNodes, reduceToSelectedNodesOnly} from "../hierarchy-utils";
    import DataTypeDecoratorTreeNode from "./DataTypeDecoratorTreeNode.svelte";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";

    export let selectionFilter = () => true;
    export let decorators = [];
    export let expanded = true;
    export let nonConcreteSelectable = false;

    const root = {name: "Root", isExpanded: true};

    let dataTypesCall = dataTypeStore.findAll();

    let qry = "";
    let searchNodes = [];

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

    $: decoratorsByDataTypeId = _.keyBy(decorators, d => d.dataTypeId);

    $: allDataTypes = _.map(
        $dataTypesCall.data,
        d => Object.assign({}, d, {decorator: _.get(decoratorsByDataTypeId, d.id)}));

    $: requiredNodes = reduceToSelectedNodesOnly(allDataTypes, _.keys(decoratorsByDataTypeId));
    $: searchNodes = prepareSearchNodes(requiredNodes);

    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);

</script>

<SearchInput bind:value={qry}/>

<div style="padding-top: 1em">
    <DataTypeDecoratorTreeNode {selectionFilter}
                               isRoot={true}
                               node={root}
                               childNodes={displayedHierarchy}
                               {expanded}
                               {nonConcreteSelectable}
                               on:select/>
</div>

