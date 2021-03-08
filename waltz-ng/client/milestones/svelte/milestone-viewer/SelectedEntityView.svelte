<script>

    import {measurablesById, selectedMeasurable} from "./stores/measurables";

    export let data;

    let values;

    $: values = _
        .chain(data)
        .filter(d => d.id_a === $selectedMeasurable.id)
        .map(d => Object.assign({},
            {
                subChart: $measurablesById[d.id_b],
                name: d.milestone_name,
                date: d.milestone_date
            }))
        .orderBy(['subChart', 'date'], ['asc', 'desc'])
        .value();

    $: console.log({values});
    $: console.log($selectedMeasurable);
    $: console.log({data});

</script>


<h4>
    <a href="">{$selectedMeasurable.name}</a>
</h4>
<table class="table table-condensed small">
    <thead>
    <th>Venue</th>
    <th>Milestone</th>
    <th>Date</th>
    </thead>
    <tbody>
    {#each values as milestone}
    <tr>
        <td>{milestone.subChart.name}</td>
        <td>{milestone.name}</td>
        <td>{milestone.date}</td>
    </tr>
    {/each}
    </tbody>
</table>
