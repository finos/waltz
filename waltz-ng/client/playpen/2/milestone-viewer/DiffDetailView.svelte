<script>
    import {dynamicDate, fixedDate} from "./stores/selected-dates";
    import {findStrata, prettyDate} from "../milestone-utils";

    export let data;
    export let measurablesById;

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
            .map((v, k) => ({ k: Number(k), r1: v, r2: bMap[k] || null}))
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
                changes: _.map(v, d => measurablesById[d.k])
            }))
            .value();

        return groupedChanges;
    }

    let strata = null;
    let t1;
    let t2;

    $: [t1, t2] = _.orderBy([$fixedDate, $dynamicDate], t => t.getTime());

    $: t1Strata = findStrata(data, t1);
    $: t2Strata = findStrata(data, t2);

    let diffReports = [];

    $: {
        const zipped = _.zipWith(
            t1Strata,
            t2Strata,
            (d1, d2) => ({
                m: measurablesById[Number(d1.k)],
                t1: d1.stratum?.values,
                t2: d2.stratum?.values}));

        console.log({zipped, t1Strata, t2Strata});

        diffReports = _.map(zipped, d => Object.assign({}, d, {diff: mkDiff(d.t1, d.t2)}));
    }

    const niceName = {
        g: "Buy",
        r: "Sell",
        a: "Hold"
    };
</script>

<h3>{prettyDate(t1)} &raquo; {prettyDate(t2)}</h3>

{#each diffReports as summary}
    <h4>{summary.m.name}</h4>

    <table class="table table-condensed small">
        <thead>
        <th>From</th>
        <th>To</th>
        <th>Count</th>
        </thead>
        <tbody>
        {#each summary.diff as row}
        <tr>
            <td class={`rating-${row.r1}`}>
                {niceName[row.r1] || "-" }
            </td>
            <td class={`rating-${row.r2}`}>
                {niceName[row.r2] || "-" }
            </td>
            <td>
                {row.changes.length}
            </td>
        </tr>
        {/each}
        </tbody>
    </table>
{/each}





<style type="text/scss">
    @import "../../../../style/variables";
    .rating-a {
        background: $waltz-amber-background;
    }
    .rating-r {
        background: $waltz-red-background;
    }
    .rating-g {
        background: $waltz-green-background;
    }
</style>
