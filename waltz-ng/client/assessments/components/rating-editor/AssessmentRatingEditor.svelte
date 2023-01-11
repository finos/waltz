<script>

    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import RatingListView from "./RatingListView.svelte";
    import RatingEditView from "./RatingDetailView.svelte";
    import RemoveRatingConfirmationPanel from "./RemoveRatingConfirmationPanel.svelte";
    import RatingAddView from "./RatingAddView.svelte";
    import {permissions, primaryEntityReference, selectedAssessment} from "./rating-store";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {cardinality} from "../../../common/services/enums/cardinality";
    import _ from "lodash";

    const Modes = {
        LIST: "LIST",
        EDIT: "EDIT",
        REMOVE: "REMOVE",
        ADD: "ADD"
    }

    let permissionsCall;

    $: {
        if ($selectedAssessment) {
            permissionsCall = assessmentRatingStore.findRatingPermissions($primaryEntityReference, $selectedAssessment?.definition.id, true);
        }
    }

    $: $permissions = $permissionsCall?.data;

    let activeMode = Modes.LIST;

</script>


{#if $selectedAssessment}
    <SubSection>
        <div slot="header">
            {$selectedAssessment?.definition.name}
        </div>
        <div slot="content">
            <h4>
                Assessment Detail:
            </h4>
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
            <p class="help-block">
                Cardinality: {_.get(cardinality, [$selectedAssessment?.definition.cardinality, "name"], "Unknown")}
            </p>

            <hr>

            {#if activeMode === Modes.LIST}
                <RatingListView onEdit={() => activeMode = Modes.EDIT}
                                onAdd={() => activeMode = Modes.ADD}/>
            {:else if activeMode === Modes.EDIT}
                <RatingEditView onCancel={() => activeMode = Modes.LIST}
                                onRemove={() => activeMode = Modes.REMOVE}/>
            {:else if activeMode === Modes.ADD}
                <RatingAddView onCancel={() => activeMode = Modes.LIST}/>
            {:else if activeMode === Modes.REMOVE}
                <RemoveRatingConfirmationPanel onCancel={() => activeMode = Modes.LIST}/>
            {/if}
        </div>
    </SubSection>
{:else}
    <NoData type="info">
        <Icon name="info-circle"/>
        Select an assessment on the left to view more detail and edit it's rating/s
    </NoData>
{/if}