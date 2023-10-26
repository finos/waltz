<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {flowClassificationRuleStore} from "../../../svelte-stores/flow-classification-rule-store";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {onMount} from "svelte";
    import AssessmentFavouritesList
        from "../../../assessments/components/favourites-list/AssessmentFavouritesList.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import {activeSections} from "../../../dynamic-section/section-store";
    import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";

    export let primaryEntityRef;

    const unknownRating = {
        name: "? Not found ?",
        description: "Rating not found",
        color: "#ccc"
    };

    let datatypes = [];
    let datatypesById = {};
    let datatype = null;
    let datatypeName = null;
    let rating = null;

    let datatypesCall = null;
    let rulesCall = null;
    let classificationsCall = null;

    onMount(() => {
        datatypesCall = dataTypeStore.findAll();
        rulesCall = flowClassificationRuleStore.getById(primaryEntityRef.id);
        classificationsCall = flowClassificationStore.findAll();
    });

    $: classificationRule = $rulesCall?.data;
    $: classifications = $classificationsCall?.data || [];
    $: datatypes = $datatypesCall?.data || [];

    $: classificationsById = _.keyBy(classifications, d => d.id);
    $: datatype = _.find(datatypes, dt => dt.id === classificationRule?.dataTypeId);
    $: datatypeName = _.get(datatype, ["name"], "Unknown");
    $: rating = _.get(classificationsById, [classificationRule?.classificationId], unknownRating);
</script>

{#if classificationRule}
    <PageHeader icon="shield"
                name={`Flow Classification Rule: ${classificationRule?.subjectReference?.name}`}
                small={datatypeName}>
        <div slot="breadcrumbs">
            <ol class="waltz-breadcrumbs">
                <li><ViewLink state="main">Home</ViewLink></li>
                <li><ViewLink state="main.system.list">Flow Classification Rule</ViewLink></li>
                <li>
                    <EntityLink ref={classificationRule?.subjectReference}/>
                </li>
                <li><EntityLink ref={datatype}/></li>
            </ol>
        </div>
    </PageHeader>

    <div class="waltz-page-summary waltz-page-summary-attach">
        <div class="waltz-display-section">

            <div class="row">
                <div class="col-md-6">
                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Rating:
                        </div>
                        <div class="col-sm-8">
                            <span class="indicator"
                                  style={`background-color: ${rating?.color}`}>
                            </span>
                                    <span title={rating?.description}>
                                {rating?.name}
                            </span>
                        </div>
                    </div>


                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Rating Description:
                        </div>
                        <div class="col-sm-8">
                            {rating?.description || "-"}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Source Entity:
                        </div>
                        <div class="col-sm-8">
                            <EntityLink ref={classificationRule?.subjectReference}/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Datatype:
                        </div>
                        <div class="col-sm-8">
                            <EntityLink ref={datatype}/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Datatype Description:
                        </div>
                        <div class="col-sm-8">
                            {datatype?.description || "-"}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Scope:
                        </div>
                        <div class="col-sm-8">
                            <EntityLink ref={classificationRule?.vantagePointReference}/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Description:
                        </div>
                        <div class="col-sm-8">
                            {classificationRule?.description || "-"}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Provenance:
                        </div>
                        <div class="col-sm-8">
                            {classificationRule?.provenance}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            External Id:
                        </div>
                        <div class="col-sm-8">
                            {classificationRule?.externalId || "-"}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-4 waltz-display-field-label">
                            Read Only:
                        </div>
                        <div class="col-sm-8 text-muted">
                            {#if classificationRule?.isReadonly}
                                <Icon name="lock"/> This rule is read only
                            {:else}
                                <Icon name="unlock"/> This rule can be edited
                            {/if}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-12">
                            <div class="help-block pull-right">
                                Last updated: <LastEdited entity={classificationRule}/>
                            </div>
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
    </div>
{:else}
    <PageHeader icon="exclamation-triangle"
                name="Flow Classification Rule not found">
    </PageHeader>
{/if}

<style>
    .indicator {
        display: inline-block;
        height: 0.9em;
        width: 1em;
        border: 1px solid #ccc;
        border-radius: 2px;
    }
</style>