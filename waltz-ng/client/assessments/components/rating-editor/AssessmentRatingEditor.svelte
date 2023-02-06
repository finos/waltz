<script>

    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import RatingListView from "./RatingListView.svelte";
    import RatingEditView from "./RatingDetailView.svelte";
    import RemoveRatingConfirmationPanel from "./RemoveRatingConfirmationPanel.svelte";
    import RatingAddView from "./RatingAddView.svelte";
    import {
        permissions,
        primaryEntityReference,
        selectedAssessment,
        detailPanelActiveMode,
        Modes
    } from "./rating-store";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {cardinality} from "../../../common/services/enums/cardinality";
    import _ from "lodash";


    let permissionsCall;

    $: {
        if ($selectedAssessment) {
            permissionsCall = assessmentRatingStore.findRatingPermissions($primaryEntityReference, $selectedAssessment?.definition.id, true);
        }
    }

    $: $permissions = $permissionsCall?.data;


</script>


{#if $selectedAssessment}
    <SubSection>
        <div slot="header">
            {$selectedAssessment?.definition.name}
            {#if $selectedAssessment?.definition.cardinality === 'ZERO_ONE'}
                <span title="Single valued assessment. Can only have zero or one outcome">
                    <Icon name='circle'/>
                </span>
            {:else}
                <span title="Multi valued assessment. Can have multiple outcomes">
                    <Icon name='circle'/>
                    <Icon name='circle'/>
                    <Icon name='circle'/>
                </span>
            {/if}

        </div>
        <div slot="content">
            {#if $selectedAssessment?.definition.isReadOnly}
                <p class="help-block">
                    <span style="color: orange">
                        <Icon name="lock"
                              size="lg"/>
                    </span>
                    This assessment is read only
                </p>
            {/if}
            <p class="help-block">
                <Markdown text={$selectedAssessment?.definition.description}/>
            </p>

            <hr>

            {#if $detailPanelActiveMode === Modes.LIST}
                <RatingListView onEdit={() => $detailPanelActiveMode = Modes.EDIT}
                                onAdd={() => $detailPanelActiveMode = Modes.ADD}/>
            {:else if $detailPanelActiveMode === Modes.EDIT}
                <RatingEditView onCancel={() => $detailPanelActiveMode = Modes.LIST}
                                onRemove={() => $detailPanelActiveMode = Modes.REMOVE}/>
            {:else if $detailPanelActiveMode === Modes.ADD}
                <RatingAddView onCancel={() => $detailPanelActiveMode = Modes.LIST}/>
            {:else if $detailPanelActiveMode === Modes.REMOVE}
                <RemoveRatingConfirmationPanel onCancel={() => $detailPanelActiveMode = Modes.LIST}/>
            {/if}
        </div>
    </SubSection>
{:else}
    <NoData type="info">
        <Icon name="info-circle"/>
        Select an assessment on the left to view more detail and edit it's rating/s
    </NoData>
{/if}