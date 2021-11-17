<script>


    import {measurableStore} from "../../svelte-stores/measurables";
    import _ from "lodash";
    import {mkSelectionOptions} from "../../common/selector-utils";
    import {mkRef} from "../../common/entity-utils";
    import {entity} from "../../common/services/enums/entity";
    import {parseMeasurableListResponse} from "../../survey/survey-utils";
    import {buildHierarchies, reduceToSelectedNodesOnly} from "../../common/hierarchy-utils";
    import MeasurableTree from "./MeasurableTree.svelte";

    export let response;
    export let question;

    $: items = _.get(response, ["entityListResponse"], []);

    $: categoryId = _.get(question, ["qualifierEntity", "id"], null);

    $: measurableCall = categoryId && measurableStore.findMeasurablesBySelector(mkSelectionOptions(mkRef(entity.MEASURABLE_CATEGORY.key, categoryId)));
    $: measurables = $measurableCall?.data;
    $: measurablesById = _.keyBy(measurables, d => d.id);

    $: parsedMeasurables = parseMeasurableListResponse(items, measurablesById);

    $: requiredMeasurables = reduceToSelectedNodesOnly(measurables, parsedMeasurables.measurableIds);
    $: notFoundResults = _.get(parsedMeasurables.notFoundSiphon, "results", []);

    $: hierarchy = { name:"root", hideNode: true, children: buildHierarchies(requiredMeasurables, false)};

</script>

{#if !_.isEmpty(items)}
    <MeasurableTree tree={hierarchy}/>

    {#if !_.isEmpty(notFoundResults)}
        <div class="help-block small">
            The following items were associated but a match can no longer be found:
            <ul>
                {#each notFoundResults as notFoundItem}
                    <li>{notFoundItem.name}</li>
                {/each}
            </ul>
        </div>
    {/if}
{:else}
    <span class="text-muted">-</span>
{/if}
