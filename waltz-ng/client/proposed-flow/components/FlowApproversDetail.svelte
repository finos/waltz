<script>
    import Icon from "../../common/svelte/Icon.svelte";
    import NoData from "../../common/svelte/NoData.svelte";

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
    <!-- SOURCE APPROVERS -->
    {#if sourceApprovalsFormatted.length > 0}
        <div class="accordion-item">
            <button
                class="accordion-header"
                on:click={() => sourceOpen = !sourceOpen}
                aria-expanded={sourceOpen}
            >
                <span class="section-title">Source Approvers</span>
                <span class="accordion-arrow" aria-hidden="true">{sourceOpen ? "▲" : "▼"}</span>
            </button>

            {#if sourceOpen}
                <div class="accordion-panel">
                    {#if sourceApprovalsFormatted.length === 0}
                        <NoData type="warning">
                            <Icon name="exclamation-triangle" />
                            No source approvers available.
                        </NoData>
                    {:else}
                        <div class="waltz-sub-section-content">
                            <div style="padding: 5px 0">
                                <table class="waltz-field-table waltz-field-table-border" style="width: 100%">
                                    <colgroup>
                                        <col style="width: 30%;" />
                                        <col style="width: 70%;" />
                                    </colgroup>
                                    <tbody>
                                        {#each sourceApprovalsFormatted as item (item.roleName)}
                                            <tr>
                                                <td class="wft-label" style="padding: 3px;">
                                                    <span class="role-name">{item.roleName}</span>
                                                </td>
                                                <td>
                                                    <div class="person-link-div">
                                                        {#if item.persons.length > 0}
                                                            {#each item.persons as person, idx}
                                                                <a href={`person/id/${person.person.id}`} class="person-link">
                                                                    {person.person.displayName}
                                                                </a>
                                                                <a
                                                                    href={`mailto:${person.person.email}`}
                                                                    title="Email {person.person.email}"
                                                                    style="margin: 0"

                                                                >
                                                                    <Icon name="envelope-o" />
                                                                </a>
                                                                {#if idx < item.persons.length - 1}<span>, </span>{/if}
                                                            {/each}
                                                        {:else}
                                                            <span class="wft-label small">Not defined</span>
                                                        {/if}
                                                    </div>
                                                </td>
                                            </tr>
                                        {/each}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    {/if}
                </div>
            {/if}
        </div>
    {/if}

    <!-- TARGET APPROVERS -->
    {#if targetApprovalsFormatted.length > 0}
        <div class="accordion-item">
            <button
                class="accordion-header"
                on:click={() => targetOpen = !targetOpen}
                aria-expanded={targetOpen}
            >
                <span class="section-title">Target Approvers</span>
                <span class="accordion-arrow" aria-hidden="true">{targetOpen ? "▲" : "▼"}</span>
            </button>

            {#if targetOpen}
                <div class="accordion-panel">
                    {#if targetApprovalsFormatted.length === 0}
                        <NoData type="warning">
                            <Icon name="exclamation-triangle" />
                            No target approvers available.
                        </NoData>
                    {:else}
                        <div class="waltz-sub-section-content">
                            <div style="padding: 5px 0">
                                <table class="waltz-field-table waltz-field-table-border" style="width: 100%">
                                    <colgroup>
                                        <col style="width: 30%;" />
                                        <col style="width: 70%;" />
                                    </colgroup>
                                    <tbody>
                                        {#each targetApprovalsFormatted as item (item.roleName)}
                                            <tr>
                                                <td style="padding: 3px; ">
                                                    <span class="role-name">{item.roleName}</span>
                                                </td>
                                                <td>
                                                    <div>
                                                        {#if item.persons.length > 0}
                                                            {#each item.persons as person, idx}
                                                                <span class="person">
                                                                    <a href={`person/id/${person.person.id}`} class="person-link">
                                                                        {person.person.displayName}
                                                                    </a>
                                                                    <a
                                                                        href={`mailto:${person.person.email}`}
                                                                        title="Email {person.person.email}"
                                                                        style="margin: 0"
                                                                    >
                                                                        <Icon name="envelope-o" />
                                                                    </a>
                                                                    {#if idx < item.persons.length - 1}<span>, </span>{/if}
                                                                </span>
                                                            {/each}
                                                        {:else}
                                                            <span class="wft-label small">Not defined</span>
                                                        {/if}
                                                    </div>
                                                </td>
                                            </tr>
                                        {/each}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    {/if}
                </div>
            {/if}
        </div>
    {/if}
</div>

<style>
    .approvals-container {
        width: 100%;
    }

    .accordion-item {
        margin-bottom: 12px;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
        overflow: hidden;
    }

    .accordion-header {
        width: 100%;
        background: none;
        border: none;
        outline: none;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 12px;
        font-size: 14px;
        cursor: pointer;
        color: #222;
        transition: background 0.18s;
        font-weight: 500;
    }

    .accordion-header:hover,
    .accordion-header[aria-expanded="true"] {
        background: #f5f8ff;
    }

    .section-title {
        font-weight: 600;
        color: #222;
        font-size: 14px;
    }

    .accordion-arrow {
        color: #aaa;
        font-size: 13px;
        padding-left: 12px;
        flex-shrink: 0;
    }

    .accordion-panel {
        padding: 0 12px 12px 12px;
        animation: fadeIn 0.3s;
        border-top: 1px solid #f1f1f1;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
        }

        to {
            opacity: 1;
        }
    }

    .waltz-sub-section-content {
        padding: 5px 0;
    }

    :global(.waltz-field-table) {
        border-collapse: collapse;
        font-size: 13px;
    }

    :global(.waltz-field-table td) {
        padding: 6px 8px;
        border-bottom: 1px solid #e9e9e9;
        vertical-align: top;
    }

    :global(.wft-label.small) {
        font-size: 11px;
        color: #999;
        font-weight: normal;
    }

    .person-link {
        text-decoration: none;
        font-weight: 500;
        transition: color 0.2s;
        margin-right: 4px;
    }

    .person-link:hover {
        color: #1976d2;
        text-decoration: underline;
    }

    .role-name{
        color: #5a5a5a;
        font-weight: 400;
    }

    :global(a[href^="mailto"]) {
        text-decoration: none;
        margin-right: 8px;
    }

    :global(a[href^="mailto"]:hover) {
        color: #1976d2;
    }

    @media (max-width: 600px) {
        .accordion-panel {
            padding: 0 10px 10px 10px;
        }

        .accordion-header {
            padding: 10px 10px;
            font-size: 13px;
        }

        :global(.waltz-field-table td) {
            padding: 4px 6px;
            font-size: 12px;
        }
    }
</style>