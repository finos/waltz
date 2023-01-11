<script>

    import {fade} from "svelte/transition";
    import Icon from "../../../common/svelte/Icon.svelte";
    import Tooltip from "./../../../common/svelte/Tooltip.svelte";
    import {createEventDispatcher} from "svelte";
    import _ from "lodash";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import AssessmentDefinitionTooltipContent from "./AssessmentDefinitionTooltipContent.svelte"
    import AssessmentRatingTooltipContent from "./AssessmentRatingTooltipContent.svelte"

    export let group;
    export let toggleFavourite
    export let favouriteIds

    let notProvidedCollapsed = true;

    const dispatch = createEventDispatcher();


    function selectAssessment(row) {
        dispatch("select", row);
    }

    function mkRatingTooltipProps(row) {
        return {
            rating: row.rating,
            ratingItem: row.ratingItem
        }
    }

    function mkDefinitionTooltipProps(row) {
        return {
            definition: row.definition
        }
    }

</script>

{#each group.provided as row}
    <tr transition:fade
        class="clickable">
        <td>
            <button class="btn btn-skinny"
                    on:click={() => toggleFavourite(row)}>
                <Icon size="lg"
                      name={_.includes($favouriteIds, row.definition.id) ? "star" : "star-o"}/>
            </button>
        </td>
        <td>
            <Tooltip content={AssessmentDefinitionTooltipContent}
                     placement="left-start"
                     props={mkDefinitionTooltipProps(row)}>
                <svelte:fragment slot="target">
                    <button class="btn btn-skinny"
                            on:click={() => selectAssessment(row)}>
                        <Icon name={row.definition.isReadOnly ? "lock" : "fw"}/>
                        {row.definition.name}
                    </button>
                </svelte:fragment>
            </Tooltip>
        </td>
        <td>
            <ul class="list-unstyled">
                {#each row.ratings as rating}
                    <li>
                        <Tooltip content={AssessmentRatingTooltipContent}
                                 placement="left-start"
                                 props={mkRatingTooltipProps(rating)}>
                            <svelte:fragment slot="target">
                                <RatingIndicatorCell {...rating.ratingItem}
                                                     showName="true"/>
                                {#if rating.rating.comment}
                                    <Icon name="sticky-note-o"/>
                                {/if}
                            </svelte:fragment>
                        </Tooltip>
                    </li>
                {/each}
            </ul>
        </td>
    </tr>
{/each}
{#if !_.isEmpty(group.notProvided)}
    {#if notProvidedCollapsed}
        <tr>
            <td/>
            <td class="clickable">
                <button class="btn btn-skinny"
                        on:click={() => notProvidedCollapsed = false}>
                    <Icon size="lg"
                          name="caret-right"/>
                    <i>Not Rated</i>
                </button>
            </td>
            <td class="force-wrap">
                <span class="badge">{_.size(group.notProvided)}</span>
            </td>
        </tr>
    {:else}
        <tr>
            <td/>
            <td colspan="2"
                class="clickable">
                <button class="btn btn-skinny"
                        on:click={() => notProvidedCollapsed = true}>
                    <Icon size="lg"
                          name="caret-down"/>
                    <i>Not Rated</i>
                </button>
            </td>
        </tr>
        {#each group.notProvided as row}
            <tr>
                <td>
                    <button class="btn btn-skinny"
                            on:click={() => toggleFavourite(row)}>
                        <Icon size="lg"
                              name={_.includes($favouriteIds, row.definition.id) ? "star" : "star-o"}/>
                    </button>
                </td>
                <td>
                    <Tooltip content={AssessmentDefinitionTooltipContent}
                             props={mkDefinitionTooltipProps(row)}>
                        <svelte:fragment slot="target">
                            {#if row.definition.isReadOnly}
                                <span>
                                    <Icon name="fw"/>
                                    {row.definition.name}
                                </span>
                            {:else}
                                <span>
                                    <Icon name="fw"/>
                                    <button class="btn btn-skinny"
                                            on:click={() => selectAssessment(row)}>
                                        {row.definition.name}
                                    </button>
                                </span>
                            {/if}

                        </svelte:fragment>
                    </Tooltip>
                </td>
                <td>
                    <span class="text-muted italics">Not Provided</span>
                </td>
            </tr>
        {/each}
    {/if}
{/if}
