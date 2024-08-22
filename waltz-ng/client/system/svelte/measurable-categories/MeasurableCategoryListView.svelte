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
    import {userStore} from "../../../svelte-stores/user-store";
    import systemRoles from "../../../user/system-roles";

    const Modes = {
        LIST: "list",
        DELETE: "delete"
    };

    const requiredPermissions = [  // user should have at least one of these
        systemRoles.ADMIN.key,
        systemRoles.TAXONOMY_EDITOR.key
    ];

    const permissionsCall = userStore.load();

    let loadCategoriesCall;
    let categories = [];
    let qry = "";
    let activeMode = Modes.LIST;
    let permissions = [];
    let hasEditPermissions = false;

    $: permissions = $permissionsCall?.data;

    $: hasEditPermissions = _.some(
        permissions?.roles,
        r => _.includes(requiredPermissions, r));

    function loadCategories() {
        loadCategoriesCall = measurableCategoryStore.findAll(true);
    }

    function onShowList() {
        activeMode = Modes.LIST;
    }

    onMount(() => {
        loadCategories();
    });

    $: categories = $loadCategoriesCall?.data || [];

</script>

<PageHeader icon="puzzle-piece"
            name="Measurable Categories">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Measurable Categories</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p>Measurable Categories are used to define taxonomies.</p>
        </div>
    </div>

    {#if activeMode === Modes.LIST}
        <div class="row">
            <div class="col-md-12">
                <SearchInput bind:value={qry}/>
                <table class="table table-condensed table-striped table-hover"
                       style="table-layout: fixed">
                    <thead>
                    <tr>
                        <th style="width:25%">Name</th>
                        <th style="width:25%">External Id</th>
                        <th style="width:10%">Icon</th>
                        <th style="width:10%">Allows Primary Ratings?</th>
                        <th style="width:30%">Operations</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each _.orderBy(termSearch(categories, qry), [d => d.position, d => d.name]) as category}
                        <tr>
                            <td>
                                <span title={category.description}>
                                    {category.name}
                                </span>
                            </td>
                            <td>
                                <span class="force-wrap">
                                    {category.externalId}
                                </span>
                            </td>
                            <td>
                                <span>
                                    <Icon name={category.icon}/>
                                    ({category.icon})
                                </span>
                            </td>
                            <td>
                                <Icon name={category.allowPrimaryRatings
                                    ? 'check'
                                    : 'times'}/>
                            </td>
                            <td>
                                <ul class="actions list-inline">
                                    {#if hasEditPermissions}
                                        <li>
                                            <ViewLink state="main.system.measurable-category.edit"
                                                      ctx={{id: category.id}}>
                                                Edit
                                            </ViewLink>
                                        </li>
                                    {/if}
                                    <li>
                                        <ViewLink state="main.measurable-category.list"
                                                  ctx={{id: category.id}}>
                                            View data
                                        </ViewLink>
                                    </li>
                                </ul>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="5">
                            <ViewLink state="main.system.measurable-category.create">
                                <Icon name="plus"/>
                                Create a new taxonomy
                            </ViewLink>
                        </td>
                    </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    {/if}
</div>

<style>
    .actions > li + li::before {
        content: "|";
        margin-right: 0.67em;
    }

</style>