<script>

    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import _ from "lodash";

    export let measurablesByLevel;
    export let level  = 1;

    const measurable = _.get(measurablesByLevel, level);

</script>

{#if measurable}
    <ul style="list-style-type: disc">
        <li>
            <EntityLink ref={measurable.parentReference}/>
            <svelte:self {measurablesByLevel}
                         level={level + 1}>
            </svelte:self>
        </li>
    </ul>
{/if}

