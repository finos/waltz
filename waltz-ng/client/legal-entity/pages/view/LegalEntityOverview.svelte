<script>

    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {legalEntityStore} from "../../../svelte-stores/legal-entity-store";
    import {entity} from "../../../common/services/enums/entity";
    import NoData from "../../../common/svelte/NoData.svelte";
    import AssessmentFavouritesList
        from "../../../assessments/components/favourites-list/AssessmentFavouritesList.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import {activeSections, availableSections} from "../../../dynamic-section/section-store";
    import _ from "lodash";

    export let primaryEntityReference;

    let legalEntityCall;


    $: console.log({primaryEntityReference})

    $: {
        if (primaryEntityReference) {
            legalEntityCall = legalEntityStore.getById(primaryEntityReference.id);
        }
    }

    $: legalEntity = $legalEntityCall?.data;

    function openAssessmentsSection() {

        const closedAssessmentsSection = _.find($availableSections, d => d.componentId === "assessment-rating-section");
        const openAssessmentsSection = _.find($activeSections?.sections, d => d.componentId === "assessment-rating-section");

        if (closedAssessmentsSection) {
            activeSections.add(closedAssessmentsSection);
        }

        if (openAssessmentsSection) {
            activeSections.add(openAssessmentsSection);
        }
        window.scrollTo(0, 250);
    }

</script>


<PageHeader icon={entity.LEGAL_ENTITY.icon}
            name={legalEntity?.name || "Unknown"}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                Legal Entity
            </li>
            <li>
                <span>{legalEntity?.name || "Unknown"}</span>
            </li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach"
     style="margin-bottom: 5em;">
    <div class="waltz-display-section">
        <div class="row">
            <div class="col-md-6">
                {#if legalEntity}
                    <div class="row">
                        <div class="col-sm-2">
                            Name
                        </div>
                        <div class="col-sm-10">
                            {legalEntity?.name}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            Description
                        </div>
                        <div class="col-sm-10">
                            {legalEntity?.description || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            External Id
                        </div>
                        <div class="col-sm-10">
                            {legalEntity?.externalId || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            Provenance
                        </div>
                        <div class="col-sm-10">
                            {legalEntity?.provenance}
                        </div>
                    </div>
                {:else}
                    <div class="row">
                        <div class="col-sm-12">
                            <NoData>No legal entity found</NoData>
                        </div>
                    </div>
                {/if}
            </div>
            <div class="col-md-6">
                <SubSection>
                    <div slot="header">
                        <span>Assessments</span>
                    </div>
                    <div slot="content">
                        <AssessmentFavouritesList/>
                    </div>
                    <div slot="controls">
                        <button class="btn btn-skinny pull-right btn-xs"
                                on:click={openAssessmentsSection}>
                            More
                        </button>
                    </div>
                </SubSection>
            </div>
        </div>
    </div>
</div>
