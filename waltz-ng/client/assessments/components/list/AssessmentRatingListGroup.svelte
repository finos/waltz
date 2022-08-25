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
        on:click={() => selectAssessment(row)}
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
                                    <span>
                                        <Icon name={row.definition.isReadOnly ? "lock" : "fw"}/>
                                        {row.definition.name}
                                    </span>
                </svelte:fragment>
            </Tooltip>
        </td>
        <td>
            <Tooltip content={AssessmentRatingTooltipContent}
                     placement="left-start"
                     props={mkRatingTooltipProps(row)}>
                <svelte:fragment slot="target">
                    <RatingIndicatorCell {...row.ratingItem}
                                         show-name="true"/>
                    {#if row.rating.comment}
                        <Icon name="sticky-note-o"/>
                    {/if}
                </svelte:fragment>
            </Tooltip>
        </td>
    </tr>
{/each}
{#if !_.isEmpty(group.notProvided)}
    {#if notProvidedCollapsed}
        <tr>
            <td>
                <button class="btn btn-skinny"
                        on:click={() => notProvidedCollapsed = false}>
                    <Icon size="lg"
                          name="caret-right"/>
                </button>
            </td>
            <td class="clickable"
                on:click={() => notProvidedCollapsed = false}>
                <strong>Not Rated</strong>
            </td>
            <td class="force-wrap">
                <ul class="list-inline">
                    {#each group.notProvided as row}
                        {#if row.definition.isReadOnly}
                            <li class="force-wrap">
                                <span class="text-muted">
                                    <Icon name="lock"/>{row.definition.name}
                                </span>
                            </li>
                        {:else}
                            <li class="force-wrap">
                                <a class="clickable"
                                   on:click={() => selectAssessment(row)}>
                                    {row.definition.name}
                                </a>
                            </li>
                        {/if}
                    {/each}
                </ul>
            </td>
        </tr>
    {:else}
        <tr>
            <td>
                <button class="btn btn-skinny"
                        on:click={() => notProvidedCollapsed = true}>
                    <Icon size="lg"
                          name="caret-down"/>
                </button>
            </td>
            <td colspan="2"
                class="clickable"
                on:click={() => notProvidedCollapsed = true}>
                <strong>Not Rated</strong>
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
                                    <span>
                                        <Icon name={row.definition.isReadOnly ? "lock" : "fw"}/>
                                        {row.definition.name}
                                    </span>
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


<style>
    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
    }

    li::after {
        content: ", ";
    }

    li:last-child::after {
        content: "";
    }

    li {
        padding-top: 0.1em;
    }
</style>