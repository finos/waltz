<script>

    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import NoData from "../../../common/svelte/NoData.svelte";
    import AssessmentFavouritesList
        from "../../../assessments/components/favourites-list/AssessmentFavouritesList.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import {activeSections, availableSections} from "../../../dynamic-section/section-store";
    import _ from "lodash";
    import {legalEntityRelationshipStore} from "../../../svelte-stores/legal-entity-relationship-store";
    import {legalEntityRelationshipKindStore} from "../../../svelte-stores/legal-entity-relationship-kind-store";
    import Icon from "../../../common/svelte/Icon.svelte";


    export let primaryEntityReference;

    let legalEntityRelCall;
    let relKindCall;

    $: {
        if (primaryEntityReference) {
            legalEntityRelCall = legalEntityRelationshipStore.getById(primaryEntityReference.id);
        }
    }

    $: legalEntityRelationship = $legalEntityRelCall?.data;

    $: {
        if (legalEntityRelationship) {
            relKindCall = legalEntityRelationshipKindStore.getById(legalEntityRelationship?.relationshipKindId);
        }
    }

    $: relKind = $relKindCall?.data;

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


<PageHeader icon={entity.LEGAL_ENTITY_RELATIONSHIP.icon}
            name={`${relKind?.name || "Unknown relationship kind"} between ${legalEntityRelationship?.targetEntityReference?.name || "Unknown Target Entity"} and ${legalEntityRelationship?.legalEntityReference?.name || "Unknown Legal Entity"}`}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                Legal Entity Relationship
            </li>
            <li>
                <span>{legalEntityRelationship?.targetEntityReference?.name || "Unknown"}</span>
            </li>
            <li>
                <span>{legalEntityRelationship?.legalEntityReference?.name || "Unknown"}</span>
            </li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach"
     style="margin-bottom: 5em;">
    <div class="waltz-display-section">
        <div class="row">
            <div class="col-md-6">
                {#if legalEntityRelationship}
                    <div class="row">
                        <div class="col-sm-4">
                            Target Entity Name
                        </div>
                        <div class="col-sm-6">
                            {legalEntityRelationship?.targetEntityReference?.name}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            Legal Entity Name
                        </div>
                        <div class="col-sm-6">
                            {legalEntityRelationship?.legalEntityReference?.name}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            Description
                        </div>
                        <div class="col-sm-6">
                            {legalEntityRelationship?.description || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            External Id
                        </div>
                        <div class="col-sm-6">
                            {legalEntityRelationship?.externalId || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            Provenance
                        </div>
                        <div class="col-sm-6">
                            {legalEntityRelationship?.provenance}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            Read Only
                        </div>
                        <div class="col-sm-6">
                            <Icon name={legalEntityRelationship?.isReadonly ? "check" : "times"}/>
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
