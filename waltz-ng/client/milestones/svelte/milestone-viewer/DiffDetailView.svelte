<script>
    import {dynamicDate, fixedDate} from "./stores/selected-dates";
    import {findStrata, prettyDate} from "../../milestone-utils";
    import DiffDetailReport from "./DiffDetailReport.svelte";
    import {measurablesById} from "./stores/measurables";
    import Sankey from "./Sankey.svelte";
    import mkData from "./sankey-test-data";

    export let data;

    /**
     * Takes a stack summary and returns an inverted map
     *
     * Example:
     * ```
     *    invertRatings({r: [1, 2], a: [4]})
     * => {1: 'r', 2: 'r', 4: 'a'};
     * ```
     *
     * @param rs
     * @returns {*}
     */
    function invertRatings(rs) {
        return _
            .chain(rs)
            .flatMap((xs, k) => _.map(xs, x => ({k, x})))
            .reduce((acc, d) => {
                acc[d.x] = d.k;
                return acc;
            }, {})
            .value();
    }

    function mkDiff(a, b) {
        const aMap = invertRatings(a);
        const bMap = invertRatings(b);

        const moversAndLeavers = _.chain(aMap)
            .map((v, k) => ({k: Number(k), r1: v, r2: bMap[k] || null}))
            .filter(d => d.r1 !== d.r2)
            .value();

        const joiners = _
            .chain(bMap)
            .omit(_.keys(aMap)) // not in A
            .map((v, k) => ({k: Number(k), r1: null, r2: v}))
            .value();

        const changes = _.concat(moversAndLeavers, joiners)

        const groupedChanges = _
            .chain(changes)
            .groupBy(d => `${d.r1}_${d.r2}`)
            .map((v, k) => ({
                r1: v[0].r1,
                r2: v[0].r2,
                changes: _.map(v, d => $measurablesById[d.k])
            }))
            .value();

        return groupedChanges;
    }

    // let measurablesById;
    let strata = null;
    let t1;
    let t2;
    let t1Strata;
    let t2Strata;

    $: [t1, t2] = _.orderBy([$fixedDate, $dynamicDate], t => t.getTime());

    $: t1Strata = findStrata(data, t1);
    $: t2Strata = findStrata(data, t2);

    let diffReports = [];

    $: {
        const zipped = _.zipWith(
            t1Strata,
            t2Strata,
            (d1, d2) => ({
                m: $measurablesById[Number(d1.k)],
                t1: d1.stratum?.values,
                t2: d2.stratum?.values
            }));

        diffReports = _.map(zipped, d => Object.assign({}, d, {diff: mkDiff(d.t1, d.t2)}));
    }

    $: console.log({data, td: mkData(), diffReports})
</script>


<h3>{prettyDate(t1)} &raquo; {prettyDate(t2)}</h3>

{#each diffReports as summary}
    <h4>{summary.m.name}</h4>

    <div style="display: inline-block; vertical-align: top; padding-top: 2em">
        <Sankey data={mkData()}/>
    </div>
    <div style="display: inline-block; width: 70%">
        <DiffDetailReport report={summary}/>
    </div>
{/each}
