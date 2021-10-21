<script>

    import ApplicationInfoPanel from "./ApplicationInfoPanel.svelte";
    import ChangeInitiativeInfoPanel from "./ChangeInitiativeInfoPanel.svelte";
    import MeasurableInfoPanel from "./MeasurableInfoPanel.svelte";

    export let primaryEntityRef;

    function determinePanel(entityKind) {
        switch (entityKind) {
            case "APPLICATION":
                return ApplicationInfoPanel;
            case "CHANGE_INITIATIVE":
                return ChangeInitiativeInfoPanel;
            case "MEASURABLE":
                return MeasurableInfoPanel;
            default:
                throw `No info panel for kind: ${entityKind}`;
        }
    }

    $: panel = determinePanel(primaryEntityRef?.kind);

</script>

<svelte:component this={panel} {primaryEntityRef}>
    <div slot="post-header">
        <slot name="post-header"></slot>
    </div>
    <div slot="footer">
        <slot name="footer"></slot>
    </div>
</svelte:component>
