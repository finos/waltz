<script>

    import {entity} from "../../../common/services/enums/entity";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let breakdownStats = [];


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


<ul>
    {#each displayedStats as stat}
        <li>
            {stat.entityInfo.name || "Unknown"} -
            <Icon name="user"/> {stat.personCounts.activePeopleCount} / <span
            class="text-muted">{stat.personCounts.removedPeopleCount}</span>
        </li>
    {/each}
</ul>


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