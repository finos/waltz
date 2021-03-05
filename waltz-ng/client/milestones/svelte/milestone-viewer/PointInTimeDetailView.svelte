<script>
    import {dynamicDate} from "./stores/selected-dates";
    import {findStrata, prettyDate} from "../../milestone-utils";
    import PointInTimeStratumSummary from "./PointInTimeStratumSummary.svelte";

    export let data;
    export let config;

    let strata = null;

    $: measurablesById = config.measurablesById;

    $: strata = findStrata(data, $dynamicDate.getTime());

</script>

<h3>{prettyDate($dynamicDate)}</h3>

{#each strata as stratum}
    <h4>{measurablesById[Number(stratum.k)]?.name}</h4>
    <PointInTimeStratumSummary data={stratum}
                               {config}/>
{/each}




