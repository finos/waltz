<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {measurableCategoryStore} from "../../../svelte-stores/measurable-category-store";
    import {onMount} from "svelte";
    import {termSearch} from "../../../common";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {displayError} from "../../../common/error-utils";
    import toasts from "../../../svelte-stores/toast-store";

    export let categoryId;

    let loadCategoryCall;
    let workingCopy = null;

    function loadCategories() {
        loadCategoryCall = measurableCategoryStore.getById(categoryId);
    }

    function onSave() {
        measurableCategoryStore
            .save(workingCopy)
            .then(() => {
                toasts.info("Category saved successfully");
            })
            .catch(e => displayError("Failed to save category", e));
    }

    function onEditCategory(c) {
        workingCopy = Object.assign({}, c);
    }

    onMount(() => {
        loadCategories();
    });

    $: categories = $loadCategoryCall?.data || [];

</script>

<PageHeader icon="puzzle-piece"
            name="Measurable Categories">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li><ViewLink state="main.system.measurable-categories">Measurable Categories</ViewLink></li>
            <li>Measurable Category</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <h3>Edit Category</h3>
    <form autocomplete="off"
          on:submit|preventDefault={onSave}>
        <div class="form-group">
            <label for="name">Name</label>
            <input type="text"
                   class="form-control"
                   id="name"
                   bind:value={workingCopy.name}
                   placeholder="Name"
                   required>
        </div>
        <div class="form-group">
            <label for="externalId">ExternalId</label>
            <input type="text"
                   class="form-control"
                   id="externalId"
                   bind:value={workingCopy.externalId}
                   placeholder="External Id"
                   required>
            <div class="help-block">
                This is the external id used to identify this category in external systems.
                Be careful when changing this value as it may break integrations.
            </div>
        </div>
        <div class="form-group">
            <label for="description">Description</label>
            <textarea class="form-control"
                      id="description"
                      bind:value={workingCopy.description}
                      placeholder="Description"
                      rows="3"></textarea>
            <div class="help-block">
                Description of the category. Markdown is supported.
            </div>
        </div>
        <div class="form-group">
            <label for="icon">Icon Name</label>
            <div>
                <input type="text"
                       style="width: 20%; display: inline"
                       class="form-control input-sm"
                       id="icon"
                       bind:value={workingCopy.icon}
                       placeholder="Icon name e.g. cog"
                       required>
                <Icon name={workingCopy.icon}/>
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
                   bind:value={workingCopy.position}>
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
                       bind:checked={workingCopy.allowPrimaryRatings}>
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
                       bind:checked={workingCopy.editable}>
                <div class="help-inline">
                    Allow the taxonomy to be edited from within Waltz using the maintenance screen.
                </div>
            </div>
        </div>
        <div class="form-group">
            <label for="editor_role">
                Editor Role
            </label>
            <div>
                <input id="editor_role"
                       bind:value={workingCopy.ratingEditorRole}>
                <div class="help-inline">
                    Which role is needed to edit the taxonomy
                </div>
            </div>
        </div>


        <button type="submit"
                class="btn btn-primary">
            Save
        </button>
        <button type="reset"
                class="btn btn-link"
                on:click={onShowList}>
            Cancel
        </button>
    </form>
</div>


<style>
    .help-inline {
        display: inline-block;
        margin-top: 5px;
        margin-bottom: 10px;
        color: #737373;
    }
</style>