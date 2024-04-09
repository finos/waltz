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

    export let endUserApplication;

    let orgUnitCall;
    let promotedAppCall;

    let promotedApp = null;
    let orgUnit = null;

    $: {
        if (endUserApplication?.organisationalUnitId) {
            orgUnitCall = orgUnitStore.getById(endUserApplication.organisationalUnitId);
        }

        if (endUserApplication?.isPromoted) {
            promotedAppCall = applicationStore.findByExternalId(endUserApplication.externalId);
        }
    }

    $: $primaryRef = endUserApplication;  // hack to reset the assessment subsection
    $: orgUnit = $orgUnitCall?.data;
    $: promotedApp = _.find($promotedAppCall?.data || [], { applicationKind: 'EUC' });
</script>


<svelte:head>
    <title>Waltz: {endUserApplication?.name} - End User Application</title>
</svelte:head>


{#if endUserApplication}

    <PageHeader icon="table"
                name={endUserApplication.name || "End User Application"}>
        <div slot="breadcrumbs">
            <ol class="waltz-breadcrumbs">
                <li><ViewLink state="main">Home</ViewLink></li>
                <li>End User Application</li>
                <li>{endUserApplication.name}</li>
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
                        {endUserApplication.name}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        External Id
                    </div>
                    <div class="col-sm-8">
                        {endUserApplication.externalId}
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
                        {endUserApplication.applicationKind}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Lifecycle Phase
                    </div>
                    <div class="col-sm-8">
                        {endUserApplication.lifecyclePhase}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Risk Rating
                    </div>
                    <div class="col-sm-8">
                        {endUserApplication.riskRating}
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-4 waltz-display-field-label">
                        Description
                    </div>
                    <div class="col-sm-8">
                        <DescriptionFade expanderAlignment="left"
                                         text={endUserApplication.description}/>
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
                                    on:click={() => activeSections.add(dynamicSections.assessmentRatingSection)}>
                                More
                            </button>
                        </div>
                    </div>
                </SubSection>
            </div>
        </div>
    </div>
{/if}