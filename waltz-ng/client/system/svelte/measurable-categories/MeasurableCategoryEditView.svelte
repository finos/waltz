<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {measurableCategoryStore} from "../../../svelte-stores/measurable-category-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {displayError} from "../../../common/error-utils";
    import toasts from "../../../svelte-stores/toast-store";
    import {writable} from "svelte/store";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import _ from "lodash";

    export let id;
    export let mode = "EDIT";

    const ratingSchemesCall = ratingSchemeStore.loadAll();
    const DEFAULT_VALUES = {
        id: null, // we want a new category
        position: 0,
        lastUpdatedBy: "defaultValue",
        icon: "puzzle-piece",
        allowPrimaryRatings: false
    };

    let loadCategoryCall;
    let workingCopy = writable(null);
    let category = null;
    let possibleRatingSchemes = [];

    function onSave() {
        measurableCategoryStore
            .save($workingCopy)
            .then(() => {
                toasts.info("Category saved successfully");
                $pageInfo = {
                    state: "main.system.measurable-category.list",
                    params: {}
                };
            })
            .catch(e => displayError("Failed to save category", e));
    }

    $: loadCategoryCall = id && measurableCategoryStore.getById(id);

    $: {
        if (mode === "CREATE") {
            $workingCopy = {};
        }
    }

    $: {
        category = $loadCategoryCall?.data || [];
        $workingCopy = Object.assign({}, DEFAULT_VALUES, category);
    }

    $: possibleRatingSchemes = _.sortBy($ratingSchemesCall.data, d => d.name);
</script>

<PageHeader icon="puzzle-piece"
            name="Measurable Categories">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li><ViewLink state="main.system.measurable-category.list">Measurable Categories</ViewLink></li>
            <li>{mode === 'CREATE' ? 'Create' : category?.name}</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    {#if mode === 'CREATE'}
        <h3>Create Category</h3>
    {:else}
        <h3>Edit Category</h3>
    {/if}

    {#if $workingCopy}
    <form autocomplete="off"
          on:submit|preventDefault={onSave}>
        <div class="form-group">
            <label for="name" class="required">Name</label>
            <input type="text"
                   class="form-control"
                   id="name"
                   bind:value={$workingCopy.name}
                   placeholder="Name"
                   required>
        </div>
        <div class="form-group">
            <label for="externalId" class="required">External Id</label>
            <input type="text"
                   class="form-control"
                   id="externalId"
                   bind:value={$workingCopy.externalId}
                   placeholder="External Id"
                   required>
            <div class="help-block">
                This is the external id used to identify this category in external systems.
                Be careful when changing this value as it may break integrations.
            </div>
        </div>
        <div class="form-group">
            <label for="ratingScheme" class="required">Rating Scheme</label>
            <select id="ratingScheme"
                    class="form-control"
                    disabled={mode === 'EDIT'}
                    required
                    bind:value={$workingCopy.ratingSchemeId}>
                {#each possibleRatingSchemes as r}
                    <option value={r.id}>
                        {r.name}
                    </option>
                {/each}
            </select>
            <div class="help-block">
                The rating scheme is used to define the possible values for mappings to this taxonomy.
            </div>
        </div>
        <div class="form-group">
            <label for="description" class="required">Description</label>
            <textarea class="form-control"
                      id="description"
                      bind:value={$workingCopy.description}
                      placeholder="Description"
                      rows="3"></textarea>
            <div class="help-block">
                Description of the category. Markdown is supported.
            </div>
        </div>
        <div class="form-group">
            <label for="icon" class="required">Icon Name</label>
            <div>
                <input type="text"
                       style="width: 20%; display: inline"
                       class="form-control input-sm"
                       id="icon"
                       bind:value={$workingCopy.icon}
                       placeholder="Icon name e.g. cog"
                       required>
                <Icon name={$workingCopy.icon}/>
            </div>
            <div class="help-block">
                This is the icon associated to this category. Currently these icons are based upon the <a
                target="_blank" rel="noreferrer noopener" href="https://fontawesome.com/v4/icons/">FontAwesome v4
                icon set </a>.
            </div>
        </div>
        <div class="form-group">
            <label for="icon">Position</label>
            <input class="form-control"
                   type="number"
                   id="position"
                   style="width: 20%"
                   required="required"
                   placeholder="Position for this category in tabbed sections. Default order is based upon the name of the category"
                   bind:value={$workingCopy.position}>
            <div class="help-block">
                Position, used for ordering categories.
                Lower numbers go first, name is used as a tie breaker.
            </div>
        </div>


        <div class="form-group">
            <label for="allow_primary_ratings">
                Allow Primary Ratings
            </label>
            <div>
                <input id="allow_primary_ratings"
                       type="checkbox"
                       bind:checked={$workingCopy.allowPrimaryRatings}>
                <div class="help-inline">
                    Determines whether users can select one measurable in this category to be flagged as the primary
                    rating for the application.
                </div>
            </div>
        </div>

        <div class="form-group">
            <label for="editable">
                Editable
            </label>
            <div>
                <input id="editable"
                       type="checkbox"
                       bind:checked={$workingCopy.editable}>
                <div class="help-inline">
                    Allow the taxonomy to be edited from within Waltz using the maintenance screen.
                </div>
            </div>
        </div>


        <div class="form-group">
            <label for="editor_role">Ratings Editor Role</label>
            <input id="editor_role"
                   class="form-control"
                   style="width: 20%"
                   bind:value={$workingCopy.ratingEditorRole}>
            <div class="help-inline">
                The role needed by users to perform mappings against this taxonomy.
            </div>
        </div>


        <button type="submit"
                class="btn btn-primary">
            Save
        </button>
        <ViewLink state="main.system.measurable-category.list">
            Cancel
        </ViewLink>
    </form>
    {/if}
</div>


<style>
    .help-inline {
        display: inline-block;
        margin-top: 5px;
        margin-bottom: 10px;
        color: #737373;
    }

    .required:after {
        content:" *";
        color: red;
    }
</style>