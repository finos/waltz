<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import InvolvementKindOverview from "./InvolvementKindOverview.svelte";
    import AssessmentFavouritesList
        from "../../../assessments/components/favourites-list/AssessmentFavouritesList.svelte";
    import {activeSections} from "../../../dynamic-section/section-store";
    import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import {primaryEntityReference as primaryRef} from "../../../assessments/components/rating-editor/rating-store";

    export let parentEntityRef;

    let involvementKindCall;

    $: involvementKindCall = involvementKindStore.getById(parentEntityRef.id);
    $: involvementKind = $involvementKindCall?.data;
    $: $primaryRef = parentEntityRef;

    function reload(id) {
        involvementKindCall = involvementKindStore.getById(id, true);
    }

</script>

<PageHeader icon="id-badge"
            name={involvementKind?.name}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.system.list">System Admin</ViewLink>
            </li>
            <li>
                <ViewLink state="main.involvement-kind.list">Involvement Kinds</ViewLink>
            </li>
            <li>
                <span>{involvementKind?.name}</span>
            </li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">
        <div class="row">
            <div class="col-md-6">
                <InvolvementKindOverview {involvementKind} {reload}/>
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
</div>
