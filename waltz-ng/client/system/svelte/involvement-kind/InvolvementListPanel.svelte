<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";

    const Modes = {
        VIEW: "VIEW",
        BULK_UPLOAD: "BULK_UPLOAD"
    }

    let activeMode = Modes.VIEW;

    $: involvementStatCall = involvementKindStore.findUsageStats();
    $: usageStats = $involvementStatCall.data;

</script>


<h4>Hi there!</h4>