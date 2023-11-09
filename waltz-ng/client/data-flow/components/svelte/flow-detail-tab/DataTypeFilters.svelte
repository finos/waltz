<script>


    import {mkAssessmentFilter, mkDataTypeFilter, mkDataTypeFilterId} from "./flow-detail-utils";
    import _ from "lodash";
    import {filters} from "./flow-details-store";
    import DataTypeTreeSelector from "../../../../common/svelte/DataTypeTreeSelector.svelte";
    import {flattenChildren} from "../../../../common/hierarchy-utils";

    export let dataTypes = [];

    $: dataTypeIds = _.map(dataTypes, d => d.id);

    $: selectionFilter = (d) => {
        console.log({d})
        const dtFilter = _.find($filters, d => d.id = mkDataTypeFilterId());
        const filteredDataTypes = _.get(dtFilter, ["dataTypes"], []);
        return _.isEmpty(filteredDataTypes) || !_.includes(filteredDataTypes, d.id);
    }

    function selectDataType(evt) {

        console.log({evt});

        const filterId = mkDataTypeFilterId();

        const dataType = evt.detail;

        const children = flattenChildren(dataType, [dataType]);
        const dataTypesToToggle = _.map(children, d => d.id);

        console.log({children});

        const existingFilter = _.find($filters, f => f.id === filterId);

        const existingDataTypes = _.get(existingFilter, "dataTypes", []);

        const dataTypeId = dataType.id;

        const newDataTypes = _.includes(existingDataTypes, dataTypeId)
            ? _.without(existingDataTypes, ...dataTypesToToggle)
            : _.uniq(_.concat(existingDataTypes, dataTypesToToggle));

        console.log({dataTypeId, existingDataTypes, newDataTypes, inc: _.includes(existingDataTypes, dataTypeId)})

        const withoutFilter = _.reject($filters, d => d.id === filterId);

        const newFilter = mkDataTypeFilter(filterId, newDataTypes);

        $filters = _.concat(withoutFilter, newFilter);

    }

    function clearFilters() {
        const filterId = mkDataTypeFilterId();
        $filters = _.reject($filters, d => d.id === filterId);
    }

    function filterAllDataTypes() {
        const filterId = mkDataTypeFilterId();
        const newFilter = mkDataTypeFilter(filterId, dataTypeIds);
        const withoutFilter = _.reject($filters, d => d.id === filterId);
        $filters = _.concat(withoutFilter, newFilter);
    }

    function mkSelectionFilter(filters) {
        const dtFilter = _.find(filters, d => d.id = mkDataTypeFilterId());
        const filteredDataTypes = _.get(dtFilter, ["dataTypes"], []);
        console.log({filteredDataTypes});
        return d => _.includes(filteredDataTypes, d.id);
    }


</script>

<div class="help-block"
 style="padding-top: 1em">
Use the data types to filter the logical flows. Selecting a datatype will add or remove it from the filter, along with all of its children.
</div>
<DataTypeTreeSelector multiSelect={true}
                      expanded={true}
                      dataTypeIds={dataTypeIds}
                      nonConcreteSelectable={true}
                      selectionFilter={mkSelectionFilter($filters)}
                      on:select={selectDataType}/>

<div style="padding-top: 1em">
    <button class="btn btn-skinny"
            on:click={filterAllDataTypes}>
        Deselect All
    </button>
    |
    <button class="btn btn-skinny"
            on:click={clearFilters}>
        Select All
</button>
</div>

<style>


</style>