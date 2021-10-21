<script>

    import ApplicationInfoPanel from "./ApplicationInfoPanel.svelte";
    import ChangeInitiativeInfoPanel from "./ChangeInitiativeInfoPanel.svelte";

    export let primaryEntityRef;

    function determinePanel(entityKind) {
        switch (entityKind) {
            case "APPLICATION":
                return ApplicationInfoPanel;
            case "CHANGE_INITIATIVE":
                return ChangeInitiativeInfoPanel;
            default:
                throw `No info panel for kind: ${entityKind}`;
        }
    }

    $: panel = determinePanel(primaryEntityRef?.kind);

</script>

<svelte:component this={panel} {primaryEntityRef}/>
