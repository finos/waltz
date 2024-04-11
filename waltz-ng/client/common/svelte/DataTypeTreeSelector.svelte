<script>
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {buildHierarchies, doSearch, prepareSearchNodes, reduceToSelectedNodesOnly} from "../hierarchy-utils";
    import DataTypeTreeNode from "./DataTypeTreeNode.svelte";
    import SearchInput from "./SearchInput.svelte";
    import _ from "lodash";
    import {flowClassificationStore} from "../../svelte-stores/flow-classification-store";

    export let multiSelect = false;
    export let nonConcreteSelectable = true;
    export let selectionFilter = () => true;
    export let expanded = true;
    export let dataTypeIds = [];
    export let ratingCharacteristics = [];
    export let usageCharacteristics = [];

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
    let classificationsCall = flowClassificationStore.findAll();
    let qry = "";
    let searchNodes = [];
    let dataTypes = [];

    $: dataTypes = $dataTypesCall.data;

    $: enrichedDataTypes = _.map(dataTypes, d => {
        const ucs = _.get(usageCharacteristicsByDatatypeId, d.id);
        const rcs = _.get(ratingCharacteristicsByDatatypeId, d.id);

        if (rcs){
            rcs.sourceOutboundClassification = _.get(classificationsByCode, rcs?.sourceOutboundRating);
            rcs.targetInboundClassification = _.get(classificationsByCode, rcs?.targetInboundRating);
        }

        return Object.assign({}, d, {ratingCharacteristics: rcs, usageCharacteristics: ucs})
    })

    $: classifications = $classificationsCall?.data || [];
    $: classificationsByCode = _.keyBy(classifications, d => d.code);
    $: requiredNodes = _.isEmpty(dataTypeIds)
        ? enrichedDataTypes
        : reduceToSelectedNodesOnly(enrichedDataTypes, dataTypeIds);
    $: searchNodes = prepareSearchNodes(requiredNodes);
    $: displayedHierarchy = calcDisplayHierarchy(searchNodes, qry);

    $: ratingCharacteristicsByDatatypeId = _.keyBy(ratingCharacteristics, d => d.dataTypeId);
    $: usageCharacteristicsByDatatypeId = _.keyBy(usageCharacteristics, d => d.dataTypeId);

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

