<script>

    import ApplicationInfoPanel from "./ApplicationInfoPanel.svelte";
    import ChangeInitiativeInfoPanel from "./ChangeInitiativeInfoPanel.svelte";
    import MeasurableInfoPanel from "./MeasurableInfoPanel.svelte";
    import ActorInfoPanel from "./ActorInfoPanel.svelte";
    import DataTypeInfoPanel from "./DataTypeInfoPanel.svelte";
    import LogicalDataFlowPanel from "./LogicalDataFlowInfoPanel.svelte"
    import SurveyInstanceInfoPanel from "./SurveyInstanceInfoPanel.svelte";
    import PersonInfoPanel from "./PersonInfoPanel.svelte";
    import EndUserApplicationInfoPanel from "./EndUserApplicationInfoPanel.svelte";

    export let primaryEntityRef;

    function determinePanel(entityKind) {
        switch (entityKind) {
            case "ACTOR":
                return ActorInfoPanel;
            case "APPLICATION":
                return ApplicationInfoPanel;
            case "CHANGE_INITIATIVE":
                return ChangeInitiativeInfoPanel;
            case "DATA_TYPE":
                return DataTypeInfoPanel;
            case "END_USER_APPLICATION":
                return EndUserApplicationInfoPanel;
            case "LOGICAL_DATA_FLOW":
                return LogicalDataFlowPanel;
            case "MEASURABLE":
                return MeasurableInfoPanel;
            case "PERSON":
                return PersonInfoPanel;
            case "SURVEY_INSTANCE":
                return SurveyInstanceInfoPanel;
            default:
                throw `No info panel for kind: ${entityKind}`;
        }
    }

    $: panel = determinePanel(primaryEntityRef?.kind);

</script>

<svelte:component this={panel} {primaryEntityRef}>
    <div slot="post-title">
        <slot name="post-title"></slot>
    </div>
    <div slot="post-header">
        <slot name="post-header"></slot>
    </div>
    <div slot="footer">
        <slot name="footer"></slot>
    </div>
</svelte:component>
