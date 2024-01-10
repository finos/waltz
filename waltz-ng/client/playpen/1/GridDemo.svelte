<script>
    import {SlickGrid} from "slickgrid";
    import {applicationStore} from "../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../common/selector-utils";
    import {mkRef} from "../../common/entity-utils";
    import {orgUnitStore} from "../../svelte-stores/org-unit-store";
    import {
        mkEntityLinkFormatter,
        mkSortFn,
        mkAssessmentAndCategoryColumns,
        mkApplicationKindFormatter,
        mkLifecyclePhaseFormatter
    } from "../../common/slick-grid-utils";
    import {termSearch} from "../../common";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import {nestedEnums} from "../../physical-flows/svelte/physical-flow-editor-store";
    import {nestEnums} from "../../common/svelte/enum-utils";
    import {cmp} from "../../common/sort-utils";


    let elem = null;

    let appViewCall = applicationStore.getViewBySelector(mkSelectionOptions(mkRef('MEASURABLE', 80873), "CHILDREN"));
    let orgUnitCall = orgUnitStore.loadAll();
    let enumsCall = enumValueStore.load();

    let grid;
    let percentCutoff = 100;
    let searchStr = "";

    const baseColumns = [
        {
            id: "name",
            name: "Name",
            field: "application",
            sortable:  true,
            width: 180,
            formatter: mkEntityLinkFormatter(null, false),
            sortFn: (a, b) => cmp(a?.application.name, b?.application.name)
        },
        {id: "assetCode", name: "Asset Code", field: "application_assetCode", sortable:  true},
        {id: "kind", name: "Kind", field: "application_applicationKind", sortable:  true, formatter: mkApplicationKindFormatter()},
        {
            id: "orgUnit",
            name: "Org Unit",
            field: "organisationalUnit",
            sortable: true,
            width: 180,
            formatter: mkEntityLinkFormatter(null, false),
            sortFn: (a, b) => cmp(a?.organisationalUnit.name, b?.organisationalUnit.name)
        },
        {id: "lifecyclePhase", name: "Lifecycle Phase", field: "application_lifecyclePhase", width: 90, sortable:  true, formatter: mkLifecyclePhaseFormatter() }
    ];

    const options = {
        enableCellNavigation: false,
        enableColumnReorder: false
    };


    let viewData = [];
    let orgUnitsById = {};
    let columns = baseColumns;


    //
    function initGrid() {
        const elemId = "#myGrid";
        grid = new SlickGrid(elemId, [], columns, options);
        grid.onSort.subscribe((e, args) => {
            const sortCol = args.sortCol;
            grid.data.sort(mkSortFn(sortCol, args.sortAsc));
            grid.invalidate();
        });
    }

    $: {
        const data  = termSearch(
            viewData,
            searchStr,
            [
                "application.name",
                "application.assetCode",
                "application.lifecyclePhase",
                "application.kind",
                "organisationalUnit.name",
                d => _
                    .chain(_.keys(d))
                    .filter(k => k.startsWith("measurable_category"))
                    .map(k => d[k].measurable.name)
                    .join(" ")
                    .value(),
                d => _
                    .chain(_.keys(d))
                    .filter(k => k.startsWith("assessment_definition"))
                    .map(k => d[k].rating.name)
                    .join(" ")
                    .value(),
            ]);
        if (grid) {
            grid.data = data;
            grid.invalidate();
        }
    }

    $: {
        if ($appViewCall?.data) {
            const {applications, primaryAssessments, primaryRatings} = $appViewCall.data;

            const measurableRatingsByAppId = _.groupBy(primaryRatings.measurableRatings, d => d.entityReference.id);
            const assessmentRatingsByAppId = _.groupBy(primaryAssessments.assessmentRatings, d => d.entityReference.id);
            const measurablesById = _.keyBy(primaryRatings.measurables, d => d.id);
            const measurableCategoriesById = _.keyBy(primaryRatings.measurableCategories, d => d.id);
            const assessmentDefinitionsById = _.keyBy(primaryAssessments.assessmentDefinitions, d => d.id);
            const assessmentRatingsSchemeItemsById = _.keyBy(primaryAssessments.ratingSchemeItems, d => d.id);

            columns = _.concat(
                baseColumns,
                mkAssessmentAndCategoryColumns(
                    primaryAssessments.assessmentDefinitions,
                    primaryRatings.measurableCategories));

            viewData = _
                .chain(applications)
                .map(app => {
                        const base = {
                            application: app,
                            application_assetCode: app.assetCode,
                            application_applicationKind: app.applicationKind,
                            application_lifecyclePhase: app.lifecyclePhase,
                            organisationalUnit: orgUnitsById[app.organisationalUnitId],
                        };
                        const measurableRatings = measurableRatingsByAppId[app.id] || [];
                        const assessmentRatings = assessmentRatingsByAppId[app.id] || [];
                        const pmrs = _
                            .chain(measurableRatings)
                            .map(mr => {
                                const measurable = measurablesById[mr.measurableId];
                                const measurableCategory = measurableCategoriesById[measurable.categoryId];
                                return {
                                    measurable,
                                    measurableCategory,
                                }
                            })
                            .reduce((acc, d) => {acc['measurable_category/'+d.measurableCategory.externalId] = d; return acc;}, {})
                            .value();

                        const pars = _
                            .chain(assessmentRatings)
                            .map(ar => {
                                const assessmentDefinition = assessmentDefinitionsById[ar.assessmentDefinitionId];
                                const rating = assessmentRatingsSchemeItemsById[ar.ratingId];
                                return {
                                    assessmentDefinition,
                                    rating
                                }
                            })
                            .reduce((acc, d) => {acc['assessment_definition/'+d.assessmentDefinition.externalId] = d; return acc;}, {})
                            .value();

                        return _.merge(base, pmrs, pars);
                    })
                .value();

            initGrid();
        }
    }
    $: orgUnitsById = _.keyBy($orgUnitCall.data, d => d.id);
    $: $nestedEnums = nestEnums($enumsCall.data);


</script>


<h1>Hello World</h1>

<label for="percentCutoff">
    % Cutoff
    <input type="range"
           min="0"
           max="100"
           id="percentCutoff"
           bind:value={percentCutoff}>
</label>

<label for="grid-search">
    Search
    <input id="grid-search"
           type="search"
           placeholder="Search..."
           bind:value={searchStr}>
</label>

<div id="myGrid"
     class="slick-container"
     style="width:100%;height:500px;"
     bind:this={elem}/>


<style type="text/scss">
    @import "slickgrid/dist/styles/css/slick-alpine-theme.css";
    //@import "slickgrid/dist/styles/css/example-demo.css";
</style>