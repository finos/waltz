<script>

    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import {measurableRatingStore} from "../../svelte-stores/measurable-rating-store";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import SubSection from "../../common/svelte/SubSection.svelte";
    import AssessmentFavouritesList from "../../assessments/components/favourites-list/AssessmentFavouritesList.svelte";
    import {activeSections} from "../../dynamic-section/section-store";
    import {dynamicSections} from "../../dynamic-section/dynamic-section-definitions";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import Icon from "../../common/svelte/Icon.svelte";
    import DescriptionFade from "../../common/svelte/DescriptionFade.svelte";

    export let primaryEntityReference;

    let measurableRatingCall;

    $: {
        if (primaryEntityReference) {
            measurableRatingCall = measurableRatingStore.getViewById(primaryEntityReference.id);
        }
    }

    $: view = $measurableRatingCall?.data;
    $: allocations = view?.allocations || [];
    $: allocationsBySchemeId = _.keyBy(view?.allocations, d => d.schemeId);

</script>


<PageHeader icon="puzzle-piece"
            name={primaryEntityReference?.name || "Measurable Rating"}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li>Measurable Rating</li>
            {#if view}
                <li><EntityLink ref={view?.application}/></li>
                <li><EntityLink ref={view?.measurable}/></li>
            {/if}
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-6">
            <div class="row">
                <div class="col-sm-4 waltz-display-field-label">
                    Entity
                </div>
                <div class="col-sm-8">
                    <EntityLink ref={view?.application}/>
                    <div class="help-block">
                        <DescriptionFade text={view?.application.description}
                                         expanderAlignment="right"/>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 waltz-display-field-label">
                    Category
                </div>
                <div class="col-sm-8">
                    {#if view?.category}
                        <Icon name={view?.category.icon}/> {view?.category.name}
                    {:else}
                        Unknown
                    {/if}
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 waltz-display-field-label">
                    Measurable
                </div>
                <div class="col-sm-8">
                    {#if view?.measurable}
                        <EntityLink ref={view?.measurable}/>
                        <div class="help-block">
                            <DescriptionFade text={view?.measurable.description}
                                             expanderAlignment="right"/>
                        </div>
                    {:else}
                        Unknown
                    {/if}
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 waltz-display-field-label">
                    Rating
                </div>
                <div class="col-sm-8">
                    {#if view?.rating}
                        <RatingIndicatorCell {...view?.rating}
                                             showName="true"/>
                    {:else}
                        {view?.measurableRating.rating}
                    {/if}
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 waltz-display-field-label">
                    Comment
                </div>
                <div class="col-sm-8">
                    <div>
                        <DescriptionFade text={view?.measurableRating.description}
                                         expanderAlignment="right"/>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 waltz-display-field-label">
                    Primary
                </div>
                <div class="col-sm-8">
                    {#if view?.measurableRating.isPrimary}
                        <Icon name="star"/> Primary
                    {:else}
                        Not primary
                    {/if}
                </div>
            </div>
            {#if !_.isEmpty(view?.allocationSchemes)}
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Allocation
                    </div>
                    <div class="col-sm-8">
                        <table class="table table-condensed small">
                            <thead>
                            <tr>
                                <th>Scheme</th>
                                <th>Allocation</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                {#each view?.allocationSchemes as scheme}
                                    {@const alloc = _.get(allocationsBySchemeId, scheme.id)}
                                    <td>
                                        {scheme.name}
                                    </td>
                                    <td>
                                        {#if _.isEmpty(alloc)}
                                            <i>Unallocated</i>
                                        {:else}
                                            {`${alloc.percentage}%`}
                                        {/if}
                                    </td>
                                {/each}
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            {/if}
            {#if !_.isEmpty(view?.decommission)}
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Decommission Date
                    </div>
                    <div class="col-sm-8">
                        {view?.decommission.plannedDecommissionDate}
                    </div>
                </div>
            {/if}
            {#if !_.isEmpty(view?.replacements)}
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Replacement Applications
                    </div>
                    <div class="col-sm-8">
                        <table class="table table-condensed small">
                            <thead>
                            <tr>
                                <th>Commission Date</th>
                                <th>Application</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                {#each view?.replacements as replacement}
                                    <td>
                                        {replacement.plannedCommissionDate}
                                    </td>
                                    <td>
                                        <EntityLink ref={replacement.entityReference}/>
                                    </td>
                                {/each}
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            {/if}
        </div>
        <div class="col-md-6">
            <SubSection>
                <div slot="header">
                    Assessments
                </div>
                <div slot="content">
                    <AssessmentFavouritesList/>
                </div>
                <div slot="controls">
                    <div style="float: right; padding-top: 1px">
                        <button class="btn-link"
                                on:click={() =>  activeSections.add(dynamicSections.assessmentRatingSection)}>
                            More
                        </button>
                    </div>
                </div>
            </SubSection>
        </div>
    </div>
</div>

<style>

    .row {
        padding: 0.2em 0;
    }

</style>