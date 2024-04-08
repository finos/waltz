<script>
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import AssessmentFavouritesList from "../../assessments/components/favourites-list/AssessmentFavouritesList.svelte";
    import {primaryEntityReference as primaryRef} from "../../assessments/components/rating-editor/rating-store";
    import {dynamicSections} from "../../dynamic-section/dynamic-section-definitions";
    import {activeSections} from "../../dynamic-section/section-store";
    import SubSection from "../../common/svelte/SubSection.svelte";
    import {orgUnitStore} from "../../svelte-stores/org-unit-store";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import DescriptionFade from "../../common/svelte/DescriptionFade.svelte";
    import {applicationStore} from "../../svelte-stores/application-store";
    import Icon from "../../common/svelte/Icon.svelte";

    export let primaryEntityReference;

    let orgUnitCall;
    let promotedAppCall;

    let promotedApp = null;
    let orgUnit = null;

    $: {
        if (primaryEntityReference?.organisationalUnitId) {
            orgUnitCall = orgUnitStore.getById(primaryEntityReference.organisationalUnitId);
        }

        if (primaryEntityReference?.isPromoted) {
            promotedAppCall = applicationStore.findByExternalId(primaryEntityReference.externalId);
        }
    }

    $: $primaryRef = primaryEntityReference;  // hack to reset the assessment subsection
    $: orgUnit = $orgUnitCall?.data;
    $: promotedApp = _.find($promotedAppCall?.data || [], { applicationKind: 'EUC' });
</script>

<svelte:head>
    <title>Waltz: {primaryEntityReference?.name} - End User Application</title>
</svelte:head>

{#if primaryEntityReference}

    <PageHeader icon="table"
                name={primaryEntityReference.name || "End User Application"}>
        <div slot="breadcrumbs">
            <ol class="waltz-breadcrumbs">
                <li><ViewLink state="main">Home</ViewLink></li>
                <li>End User Application</li>
                <li>{primaryEntityReference.name}</li>
            </ol>
        </div>
    </PageHeader>


    <div class="waltz-page-summary waltz-page-summary-attach">
        {#if promotedApp}
            <div class="row">
                <div class="col-md-12">
                    <div class="alert alert-warning">
                        <Icon name="exclamation-triangle"/>
                        This End User Application record has been converted to a full Application record.
                        <br>
                        See: <EntityLink ref={promotedApp}/>
                    </div>
                </div>
            </div>
        {/if}
        <div class="row">
            <div class="col-md-6">
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Name
                    </div>
                    <div class="col-sm-8">
                        {primaryEntityReference.name}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        External Id
                    </div>
                    <div class="col-sm-8">
                        {primaryEntityReference.externalId}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Owning Org Unit
                    </div>
                    <div class="col-sm-8">
                        <EntityLink ref={orgUnit}/>
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Kind
                    </div>
                    <div class="col-sm-8">
                        {primaryEntityReference.applicationKind}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Lifecycle Phase
                    </div>
                    <div class="col-sm-8">
                        {primaryEntityReference.lifecyclePhase}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Risk Rating
                    </div>
                    <div class="col-sm-8">
                        {primaryEntityReference.riskRating}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Description
                    </div>
                    <div class="col-sm-8">
                        <DescriptionFade expanderAlignment="left"
                                         text={primaryEntityReference.description}/>
                    </div>
                </div>
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

{/if}