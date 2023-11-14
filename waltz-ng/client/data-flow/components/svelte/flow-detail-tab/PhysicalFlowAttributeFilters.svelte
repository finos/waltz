<script>


    import {
        mkCriticalityFilter,
        mkCriticalityFilterId,
        mkFrequencyFilter,
        mkFrequencyFilterId,
        mkTransportKindFilter,
        mkTransportKindFilterId
    } from "./flow-detail-utils";
    import _ from "lodash";
    import {filters, updateFilters} from "./flow-details-store";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let flows = [];
    export let criticalities = [];

    let enumsCall = enumValueStore.load();

    function mapOverPhysicals(flows, attr) {
        return _.chain(flows)
            .map(d => _.get(d, ["physicalFlow", attr]))
            .uniq()
            .compact()
            .value();
    }

    $: usedFrequencies = mapOverPhysicals(flows, "frequency");
    $: usedCriticalities = mapOverPhysicals(flows, "criticality");
    $: usedTransport = mapOverPhysicals(flows, "transport");

    $: criticalities = _
        .chain($enumsCall.data)
        .filter(d => d.type === "physicalFlowCriticality")
        .filter(d => _.includes(usedCriticalities, d.key))
        .orderBy(["position", "name"])
        .value();

    $: frequencies = _
        .chain($enumsCall.data)
        .filter(d => d.type === "Frequency")
        .filter(d => _.includes(usedFrequencies, d.key))
        .orderBy(["position", "name"])
        .value();

    $: transportKinds = _
        .chain($enumsCall.data)
        .filter(d => d.type === "TransportKind")
        .filter(d => _.includes(usedTransport, d.key))
        .orderBy(["position", "name"])
        .value();

    function selectCriticality(criticality) {

        const filterId = mkCriticalityFilterId();

        const existingFilter = _.find($filters, f => f.id === filterId);

        const existingCriticalities = _.get(existingFilter, "criticalities", []);

        const newCriticalities = _.includes(existingCriticalities, criticality)
            ? _.without(existingCriticalities, criticality)
            : _.concat(existingCriticalities, [criticality]);

        const newFilter = mkCriticalityFilter(filterId, newCriticalities)

        updateFilters(filterId, newFilter);
    }

    function selectFrequency(frequency) {

        const filterId = mkFrequencyFilterId();

        const existingFilter = _.find($filters, f => f.id === filterId);

        const existingFrequencies = _.get(existingFilter, "frequencies", []);

        const newFrequencies = _.includes(existingFrequencies, frequency)
            ? _.without(existingFrequencies, frequency)
            : _.concat(existingFrequencies, [frequency]);

        const newFilter = mkFrequencyFilter(filterId, newFrequencies)

        updateFilters(filterId, newFilter);
    }


    function selectTransportKind(transportKind) {

        const filterId = mkTransportKindFilterId();

        const existingFilter = _.find($filters, f => f.id === filterId);

        const existingTransportKinds = _.get(existingFilter, "transportKinds", []);

        const newFrequencies = _.includes(existingTransportKinds, transportKind)
            ? _.without(existingTransportKinds, transportKind)
            : _.concat(existingTransportKinds, [transportKind]);

        const newFilter = mkTransportKindFilter(filterId, newFrequencies)

        updateFilters(filterId, newFilter);
    }

    function clearFrequencyFilter() {
        $filters = _.filter($filters, d => d.id !== mkFrequencyFilterId());
    }

    function clearCriticalityFilter() {
        $filters = _.filter($filters, d => d.id !== mkCriticalityFilterId());
    }

    function clearTransportKindFilter() {
        $filters = _.filter($filters, d => d.id !== mkTransportKindFilterId());
    }

    function isSelectedCriticality(filters, criticality){
        const filter = _.find(filters, d => d.id === mkCriticalityFilterId());
        const filteredCriticalities = _.get(filter, "criticalities",  []);
        return _.includes(filteredCriticalities, criticality);
    }

    function isSelectedFrequency(filters, frequency){
        const filter = _.find(filters, d => d.id === mkFrequencyFilterId());
        const filteredFrequencies = _.get(filter, "frequencies",  []);
        return _.includes(filteredFrequencies, frequency);
    }

    function isSelectedTransportKind(filters, transportKind){
        const filter = _.find(filters, d => d.id === mkTransportKindFilterId());
        const filteredTransportKinds = _.get(filter, "transportKinds",  []);
        return _.includes(filteredTransportKinds, transportKind);
    }

    $: hasCriticalityFilter = _.some($filters, d => d.id === mkCriticalityFilterId());
    $: hasFrequencyFilter = _.some($filters, d => d.id === mkFrequencyFilterId());
    $: hasTransportKindFilter = _.some($filters, d => d.id === mkTransportKindFilterId());


</script>

<div class="help-block"
 style="padding-top: 1em">
Use the physical flow attributes to filter the flows. Both logical and physical flows will be filtered.
</div>
<div style="display: flex; gap: 1em">
    <div class="filter-table">
        <table class="table table-condensed table">
            <thead>
            <tr>
                <th>Criticality
                    {#if hasCriticalityFilter}
                    <button class="btn btn-skinny"
                            on:click={clearCriticalityFilter}>
                        Clear
                    </button>
                    {/if}
                </th>
            </tr>
            </thead>
            <tbody>
            {#each criticalities as criticality}
                <tr class="clickable"
                    class:selected={isSelectedCriticality($filters, criticality.key)}
                    on:click={() => selectCriticality(criticality.key)}>
                    <td>
                        <span>{criticality.name}</span>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td>
                        <NoData type="info">There are no physical flow criticalities to filter over.</NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
    <div class="filter-table">
        <table class="table table-condensed table">
            <thead>
            <tr>
                <th>
                    Frequency
                    {#if hasFrequencyFilter}
                    <button class="btn btn-skinny"
                            on:click={clearFrequencyFilter}>
                        Clear
                    </button>
                    {/if}
                </th>
            </tr>
            </thead>
            <tbody>
            {#each frequencies as frequency}
                <tr class="clickable"
                    class:selected={isSelectedFrequency($filters, frequency.key)}
                    on:click={() => selectFrequency(frequency.key)}>
                    <td>
                        <span>{frequency.name}</span>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td>
                        <NoData type="info">There are no physical flow frequencies to filter over.</NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
    <div class="filter-table">
        <table class="table table-condensed table">
            <thead>
            <tr>
                <th>
                    Transport Kind
                    {#if hasTransportKindFilter}
                    <button class="btn btn-skinny"
                            on:click={clearTransportKindFilter}>
                        Clear
                    </button>
                    {/if}
                </th>
            </tr>
            </thead>
            <tbody>
            {#each transportKinds as transportKind}
                <tr class="clickable"
                    class:selected={isSelectedTransportKind($filters, transportKind.key)}
                    on:click={() => selectTransportKind(transportKind.key)}>
                    <td>
                        <span>{transportKind.name}</span>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td>
                        <NoData type="info">There are no physical flow transport kinds to filter over.</NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</div>

<style>

    .selected {
        background-color: #eee;
    }

    .filter-table {
        flex: 1 1 30%;
    }

</style>