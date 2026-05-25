<script>
    import Icon from "../../common/svelte/Icon.svelte";
    import NoData from "../../common/svelte/NoData.svelte";

    export let title = "";
    export let approvalsFormatted = [];
    export let isOpen = false;

    const toggleOpen = () => {
        isOpen = !isOpen;
    };
</script>

{#if approvalsFormatted.length > 0}
    <div class="accordion-item">
        <button
            class="accordion-header"
            on:click={toggleOpen}
            aria-expanded={isOpen}
        >
            <span class="section-title">{title}</span>
            <span class="accordion-arrow" aria-hidden="true">{isOpen ? "▲" : "▼"}</span>
        </button>

        {#if isOpen}
            {#if approvalsFormatted.length === 0}
                <NoData type="warning">
                    <Icon name="exclamation-triangle"/>
                    No approvers available.
                </NoData>
            {:else}
                <div
                    class="accordion-panel"
                    role="region">
                    <table class="meta-table">
                        {#each approvalsFormatted as item (item.roleName)}
                            <tr>
                                <th>{item.roleName}</th>
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
                                                    <Icon name="envelope-o"/>
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
                    </table>
                </div>
            {/if}
        {/if}
    </div>
{/if}

<style>
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
        padding: 10px 12px 12px 44px;
        animation: fadeIn 0.3s;
        border-top: 1px solid #f1f1f1;
    }

    .meta-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 12px;
        background: #fcfcfc;
        border-radius: 7px;
        box-shadow: 0 0 0.5px #e0e0e0;
        margin-bottom: 0;
    }
    .meta-table th,
    .meta-table td {
        padding: 6px 10px;
        border-bottom: 1px solid #e9e9e9;
        text-align: left;
        vertical-align: top;
        color: #222;
        font-size: 12px;
        font-weight: 400;
        border-radius: 0;
    }
    .meta-table th {
        width: 140px;
        color: #705f55;
        font-size: 12px;
        font-weight: normal;
        letter-spacing: 0.04em;
        text-align: left;
        padding-right: 6px;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
        }

        to {
            opacity: 1;
        }
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