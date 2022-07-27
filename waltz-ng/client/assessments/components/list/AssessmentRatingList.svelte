<script>

    import _ from "lodash";
    import {userPreferenceStore} from "../../../svelte-stores/user-preference-store";
    import {onMount} from "svelte";
    import {createStores} from "./assessment-rating-store";
    import AssessmentRatingListGroup from "./AssessmentRatingListGroup.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {assessments} from "../section/assessment-rating-section";


    let elem;
    let stores = null;
    let defaultPrimaryList;
    let favouriteIncludedIds;
    let favouriteExcludedIds;
    let favouriteIds;
    let setFromPreferences;

    let userPreferenceCall = userPreferenceStore.findAllForUser();

    export let primaryEntityRef = [];
    export let onSelect = (d) => console.log("selected", d);

    onMount(() => {
        userPreferenceCall = userPreferenceStore.findAllForUser();
    });


    function toggleGroup(group) {
        expansions = _.includes(expansions, group.groupName)
            ? _.without(expansions, group.groupName)
            : _.concat(expansions, group.groupName);
    }


    function selectAssessment(evt) {
        onSelect(evt.detail);
    }


    function toggleFavourite(row) {

        const isExplicitlyIncluded = _.includes($favouriteIncludedIds, row.definition.id);
        const isExplicitlyExcluded = _.includes($favouriteExcludedIds, row.definition.id);
        const isDefault = _.includes($defaultPrimaryList, row.definition.id);

        let message;

        if (isExplicitlyIncluded) {
            message = "Removing from favourite assessments"
            $favouriteIncludedIds = _.without($favouriteIncludedIds, row.definition.id);
        } else if (isExplicitlyExcluded) {
            message = "Adding to favourite assessments"
            $favouriteExcludedIds = _.without($favouriteExcludedIds, row.definition.id);
        } else if (isDefault) {
            message = "Removing from favourite assessments"
            $favouriteExcludedIds = _.concat($favouriteExcludedIds, row.definition.id);
        } else {
            message = "Adding to favourite assessments"
            $favouriteIncludedIds = _.concat($favouriteIncludedIds, row.definition.id);
        }
    }


    $: {
        if (primaryEntityRef) {
            stores = createStores(primaryEntityRef);
            defaultPrimaryList = stores.defaultPrimaryList
            favouriteIncludedIds = stores.favouriteIncludedIds,
                favouriteExcludedIds = stores.favouriteExcludedIds,
                favouriteIds = stores.favouriteIds,
                setFromPreferences = stores.setFromPreferences
        }
    }


    $: userPreferences = $userPreferenceCall.data;


    $: {
        if (userPreferences && stores) {
            setFromPreferences(userPreferences)
        }
    }


    $: expansions = _
        .chain($assessments)
        .filter(d => _.includes($favouriteIds, d.definition.id))
        .map(d => d.definition.definitionGroup)
        .uniq()
        .value();


    $: groupedAssessments = _.chain($assessments)
        .groupBy(d => d.definition?.definitionGroup)
        .map((v, k) => {

            const [notProvided, provided] = _
                .chain(v)
                .orderBy(d => d.definition.name)
                .partition(d => d.rating == null)
                .value()

            return {
                groupName: k,
                notProvided,
                provided
            }
        })
        .orderBy([d => d.groupName === "Uncategorized", d => d.groupName])
        .value();


    $: {
        if (stores) {
            $defaultPrimaryList = _
                .chain($assessments)
                .filter(a => a.definition.visibility === "PRIMARY")
                .map(r => r.definition.id)
                .value();
        }
    }

</script>


<div class="row">

    <div class="col-sm-12">

        <table class="table table-hover table-condensed">
            <colgroup>
                <col width="10%"/>
                <col width="50%"/>
                <col width="40%"/>
            </colgroup>
            {#each groupedAssessments as group}
                <tbody>
                <tr style="background-color: #eee">
                    <td>
                        <button class="btn btn-skinny"
                                on:click={() => toggleGroup(group)}>
                            <Icon size="lg"
                                  name={_.includes(expansions, group.groupName) ? "caret-down" : "caret-right"}/>
                        </button>
                    </td>
                    <td colspan="2"
                        class="clickable"
                        on:click={() => toggleGroup(group)}>
                        <strong>
                            <span>{group.groupName}</span>
                        </strong>
                    </td>
                </tr>
                {#if _.includes(expansions, group.groupName)}
                    <AssessmentRatingListGroup group={group}
                                               on:select={selectAssessment}
                                               toggleFavourite={toggleFavourite}
                                               favouriteIds={favouriteIds}/>
                {/if}
                </tbody>
            {/each}
        </table>
    </div>

</div>

