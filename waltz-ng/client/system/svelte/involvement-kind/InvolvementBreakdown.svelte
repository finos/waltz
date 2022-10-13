<script>

    import {entity} from "../../../common/services/enums/entity";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let breakdownStats = [];
    export let displayMode = "LIST"
    export let onClick = (d) => console.log("Clicking on: " + JSON.stringify(d))

    const DisplayModes = {
        LIST: "LIST",
        TABLE: "TABLE"
    }

    $: displayedStats = _
        .chain(breakdownStats)
        .groupBy(d => d.entityKind)
        .map((xs, k) => {

            const personCounts = _.reduce(
                xs,
                (acc, v) => {
                    if (v.isCountOfRemovedPeople) {
                        acc.removedPeopleCount = v.personCount;
                    } else {
                        acc.activePeopleCount = v.personCount;
                    }
                    return acc;
                },
                {removedPeopleCount: 0, activePeopleCount: 0});

            return {
                entityInfo: entity[k],
                personCounts
            }
        })
        .orderBy(d => d.entityInfo?.name)
        .value()

    $: statsByKind = _.groupBy(breakdownStats, d => d.entityKind);

</script>

{#if displayMode === DisplayModes.LIST}
    <ul>
        {#each displayedStats as stat}
            <li on:click={() => onClick(stat)}>
                {stat.entityInfo.name || "Unknown"} -
                <Icon name="user"/> {stat.personCounts.activePeopleCount} /
                <span class="text-muted">{stat.personCounts.removedPeopleCount}</span>
            </li>
        {/each}
    </ul>
{:else if displayMode === DisplayModes.TABLE}
    <table class="table table-condensed">
        <thead>
        <tr>
            <th>Entity Kind</th>
            <th>Person Count (Active/Removed)</th>
        </tr>
        </thead>
        <tbody>
        {#each displayedStats as stat}
            <tr class="clickable"
                on:click={() => onClick(stat)}>
                <td>{stat.entityInfo.name || "Unknown"}</td>
                <td>
                    {stat.personCounts.activePeopleCount} /
                    <span class="text-muted">{stat.personCounts.removedPeopleCount}</span>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}


<style>
    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
        font-size: 12px
    }

    li {
        padding-top: 0.1em;
    }
</style>