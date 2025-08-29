<script>
    import { filters } from "./filter-store";
    import Pill from "../../../../common/svelte/Pill.svelte";

    export let pillDefs = {};
    export let stateCounts = {};

    export let changePillDefs = {};
    export let changeTypeCounts = {};

    export let proposerPillDefs = {};
    export let proposerPillCounts = {};

    let showFilters = false;

    const updateStateFilters = (filterKey) => {
        const selectedStates = $filters.state.includes(filterKey);
        if (selectedStates) {
            $filters.state = $filters.state.filter(f => f !== filterKey);
        } else {
            $filters.state = [...$filters.state, filterKey];
        }
    };

    const updateChangeFilters = (filterKey) => {
        const selectedChanges = $filters.change.includes(filterKey);
        if (selectedChanges) {
            $filters.change = $filters.change.filter(f => f !== filterKey);
        } else {
            $filters.change = [...$filters.change, filterKey];
        }
    };

    const updateProposerFilters = (filterKey) => {
        const selectedUsers = $filters.proposer.includes(filterKey);
        if (selectedUsers) {
            $filters.proposer = $filters.proposer.filter(f => f !== filterKey);
        } else {
            $filters.proposer = [...$filters.proposer, filterKey];
        }
    }

    const handleStateFiltersKeyDown = (event, key) => {
        if(event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            updateStateFilters(key);
        }
    };

    const handleChangeFiltersKeyDown = (event, key) => {
        if(event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            updateChangeFilters(key);
        }
    };

    const handleProposerFiltersKeyDown = (event, key) => {
        if(event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            updateProposerFilters(key);
        }
    };

    function toggleFilters() {
        showFilters = !showFilters;
    }

    const resetFilters = () => {
        $filters.proposer = [];
        $filters.change = [];
        $filters.state = [];
    }
</script>

<div class="filter-dropdown">
    <button class="filter-dropdown-btn" on:click={toggleFilters}>
        {showFilters ? "Hide Filters" : "Show Filters"}
    </button>
    {#if showFilters}
        <div class="filter-groups single-section">
            <div class="filter-group">
                <button class="btn btn-skinny" on:click={resetFilters}>
                    ✕ Clear filters
                </button>
            </div>
            <hr class="filter-divider" />
            <div class="filter-group">
                <h4>Proposal Type</h4>
                <div class="filter-pills">
                    {#each Object.keys(proposerPillDefs) as key}
                        <div
                            role="button"
                            tabindex="0"
                            on:click={() => updateProposerFilters(key)}
                            on:keydown={(e) => handleProposerFiltersKeyDown(e, key)}>
                            <Pill pillDefs={proposerPillDefs}
                                  pillKey={key}
                                  cleanPill={!$filters.proposer.includes(key)}
                                  smallText={proposerPillCounts[key]}
                            />
                        </div>
                    {/each}
                </div>
            </div>
            <hr class="filter-divider" />
            <div class="filter-group">
                <h4>State</h4>
                <div class="filter-pills">
                    {#each Object.keys(pillDefs) as key}
                    <div
                        role="button"
                        tabindex="0"
                        on:click={() => updateStateFilters(key)}
                        on:keydown={(e) => handleStateFiltersKeyDown(e, key)}>
                        <Pill pillDefs={pillDefs}
                            pillKey={key}
                            cleanPill={!$filters.state.includes(key)}
                            smallText={stateCounts[key]}
                        />
                    </div>
                    {/each}
                </div>
            </div>
            <hr class="filter-divider" />
            <div class="filter-group">
                <h4>Operation</h4>
                <div class="filter-pills">
                    {#each Object.keys(changePillDefs) as key}
                    <div
                        role="button"
                        tabindex="0"
                        on:click={() => updateChangeFilters(key)}
                        on:keydown={(e) => handleChangeFiltersKeyDown(e, key)}>
                        <Pill pillDefs={changePillDefs}
                            pillKey={key}
                            cleanPill={!$filters.change.includes(key)}
                            smallText={changeTypeCounts[key]}
                        />
                    </div>
                    {/each}
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
.filter-groups {
    position: absolute;
    z-index: 10;
    background: white;
    box-shadow: 0.25rem 2px 8px rgba(0,0,0,0.08);
    border-radius: 8px;
    min-width: 320px;
    margin-top: 0.25rem;
    padding: 0.5rem 0;
    display: block;
}

.single-section {
    padding: 0.5rem 0;
}

.filter-group {
    background: none;
    border: none;
    border-radius: 0;
    padding: 0.5rem 1rem;
    box-shadow: none;
}

.filter-group h4 {
    margin: 0 0 0.5rem 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: #333;
}

.filter-pills {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    flex-wrap: wrap;
    max-width: 100%;
}

.filter-divider {
    border: none;
    border-top: 1px solid #e2e2e2;
    margin: 0.5rem 0;
}

.filter-dropdown {
    position: relative;
    display: inline-block;
}

.filter-dropdown-btn {
    background: #0c335a;
    color: white;
    border: none;
    border-radius: 6px;
    padding: 0.5rem 1rem;
    font-size: 1.25rem;
    cursor: pointer;
    margin-bottom: 0.5rem;
    transition: background 0.2s;
}
.filter-dropdown-btn:hover {
    background: #08223c;
}
</style>