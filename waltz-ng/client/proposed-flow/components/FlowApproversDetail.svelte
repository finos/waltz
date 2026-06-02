<script>
    import ApproverSection from "./ApproverSection.svelte";

    export let groupedSourceApprovers = null;
    export let groupedTargetApprovers = null;

    let sourceOpen = false;
    let targetOpen = false;

    // Convert grouped object to array of {role, persons}
    const formatGroupedData = (groupedData) => {
        if (!groupedData || Object.keys(groupedData).length === 0) {
            return [];
        }

        return Object.entries(groupedData).map(([involvementKind, approvers]) => ({
            roleName: involvementKind,
            persons: approvers
        }));
    };

    $: sourceApprovalsFormatted = formatGroupedData(groupedSourceApprovers)
        .sort((a, b) => a.roleName.localeCompare(b.roleName));
    $: targetApprovalsFormatted = formatGroupedData(groupedTargetApprovers)
        .sort((a, b) => a.roleName.localeCompare(b.roleName));
</script>

<div class="approvals-container">
    <ApproverSection
        title="Source Approvers"
        approvalsFormatted={sourceApprovalsFormatted}
        bind:isOpen={sourceOpen}
    />

    <ApproverSection
        title="Target Approvers"
        approvalsFormatted={targetApprovalsFormatted}
        bind:isOpen={targetOpen}
    />
</div>

<style>
    .approvals-container {
        width: 100%;
    }
</style>