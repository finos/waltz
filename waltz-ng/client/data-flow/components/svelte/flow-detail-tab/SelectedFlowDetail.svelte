<script>

    import {selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import LastEdited from "../../../../common/svelte/LastEdited.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import _ from "lodash";


    let flowClassificationCall = flowClassificationStore.findAll();
    $: flowClassifications = $flowClassificationCall?.data;
    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);

    $: console.log({flowClassifications});

    function clearSelected() {
        $selectedPhysicalFlow = null;
        $selectedLogicalFlow = null;
    }

    $: selectedLogicalFlowRef = {
        kind: 'LOGICAL_DATA_FLOW',
        id: $selectedLogicalFlow?.id,
        name: "Logical Flow"
    }

    $: selectedPhysicalFlowRef = {
        kind: 'PHYSICAL_FLOW',
        id: $selectedPhysicalFlow?.id,
        name: $selectedPhysicalFlow?.name || $selectedPhysicalFlow?.specification.name || 'unnamed'
    }

</script>


{#if $selectedLogicalFlow}
    <div class="waltz-sticky-part">
        <h4>
            <span>
                Logical Flow
                <Icon name={_.get(entity, [$selectedLogicalFlow.logicalFlow.source.kind, "icon"])}/>
                <Icon name="arrow-right"/>
                <Icon name={_.get(entity, [$selectedLogicalFlow.logicalFlow.target.kind, "icon"])}/>
            </span>
            <button class="btn btn-skinny btn-xs"
                    on:click={() => clearSelected()}>
                <Icon name="times"/>
                Clear
            </button>
        </h4>

        <table class="table table-condensed small">
            <tbody>
            <tr>
                <td style="width: 20%">Name</td>
                <td>
                    <EntityLink ref={selectedLogicalFlowRef.logicalFlow}>
                    </EntityLink>
                </td>
            </tr>
            <tr>
                <td>Source</td>
                <td>
                    <EntityLink ref={$selectedLogicalFlow.logicalFlow.source}
                                isSecondaryLink={true}/>
                </td>
            </tr>
            <tr>
                <td>Target</td>
                <td>
                    <EntityLink ref={$selectedLogicalFlow.logicalFlow.target}
                                isSecondaryLink={true}/>
                </td>
            </tr>
            </tbody>
        </table>

        <div class="small">
            <LastEdited entity={$selectedLogicalFlow}/>
        </div>

        <div style="padding-top: 2em">
            <strong>Data Types</strong>
            <ul class="list-inline small">
                {#each $selectedLogicalFlow.dataTypesForLogicalFlow as type}
                    <li style="padding-bottom: 2px">
                        <div style={`display: inline-block; height: 1em; width: 1em; border:1px solid #ccc; border-radius: 2px; background-color: ${flowClassificationsByCode[type.rating]?.color}`}>
                        </div>
                        <EntityLink ref={type.decoratorEntity}
                                    showIcon={false}>
                        </EntityLink>
                    </li>
                {/each}
            </ul>
        </div>

        <div>
            <strong>Assessments</strong>
            <table class="table table-condensed small">
                <thead>
                <tr>
                    <td>Definition</td>
                    <td>Ratings</td>
                </tr>
                </thead>
                <tbody>
                {#each $selectedLogicalFlow.ratingsByDefId as defnId, ratings}
                    <tr>
                        <td>
                            Defn
                            <!--{definitionsById[defnId].name}-->
                        </td>
                        <td>
                            <ul class="list-inline">
                                {#each ratings as rating}
                                    <li>
                                        <RatingIndicatorCell {...rating}/>
                                    </li>
                                {/each}
                            </ul>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    </div>
{/if}
<!--</div>-->
<!--<div class="flow-detail-panel"-->
<!--     ng-if="$ctrl.selectedPhysicalFlow">-->
<!--    <div class="waltz-sticky-part">-->
<!--        <h4>-->
<!--                <span>-->
<!--                    Physical Flow-->
<!--                    <waltz-icon name="{{$ctrl.selectedPhysicalFlow.source.kind | toIconName:'entity' }}"></waltz-icon>-->
<!--                    <waltz-icon name="arrow-right"></waltz-icon>-->
<!--                    <waltz-icon name="{{$ctrl.selectedPhysicalFlow.target.kind | toIconName:'entity'}}"></waltz-icon>-->
<!--                </span>-->
<!--            <button class="btn btn-skinny btn-xs"-->
<!--                    ng-click="$ctrl.onClearSelect()">-->
<!--                <waltz-icon name="times"></waltz-icon>-->
<!--                Clear-->
<!--            </button>-->
<!--        </h4>-->


<!--        <table class="table table-condensed small">-->
<!--            <tbody>-->
<!--            <tr>-->
<!--                <td style="width: 20%">Name</td>-->
<!--                <td>-->
<!--                    <waltz-entity-link popover-delay="1000"-->
<!--                                       tooltip-placement="bottom"-->
<!--                                       entity-ref="{-->
<!--                                                kind: 'PHYSICAL_FLOW',-->
<!--                                                id: $ctrl.selectedPhysicalFlow.id,-->
<!--                                                name: $ctrl.selectedPhysicalFlow.name || $ctrl.selectedPhysicalFlow.physicalSpecification.name || 'unnamed'-->
<!--                                            }">-->
<!--                    </waltz-entity-link>-->
<!--                </td>-->

<!--            </tr>-->
<!--            <tr>-->
<!--                <td>Source</td>-->
<!--                <td><waltz-entity-link entity-ref="$ctrl.selectedPhysicalFlow.source" is-secondary-link="true"></waltz-entity-link></td>-->
<!--            </tr>-->
<!--            <tr>-->
<!--                <td>Target</td>-->
<!--                <td><waltz-entity-link entity-ref="$ctrl.selectedPhysicalFlow.target" is-secondary-link="true"></waltz-entity-link></td>-->
<!--            </tr>-->
<!--            <tr>-->
<!--                <td>External Id</td>-->
<!--                <td><span ng-bind="$ctrl.selectedPhysicalFlow.externalId"></span></td>-->
<!--            </tr>-->
<!--            <tr>-->
<!--                <td>Specification</td>-->
<!--                <td><waltz-entity-link entity-ref="$ctrl.selectedPhysicalFlow.physicalSpecification" is-secondary-link="true"></waltz-entity-link></td>-->
<!--            </tr>-->
<!--            <tr>-->
<!--                <td>Specification Format</td>-->
<!--                <td>-->
<!--                    <waltz-enum-value type="'DataFormatKind'"-->
<!--                                      key="$ctrl.selectedPhysicalFlow.physicalSpecification.format"-->
<!--                                      show-icon="false"-->
<!--                                      show-popover="false">-->
<!--                    </waltz-enum-value>-->
<!--                </td>-->
<!--            </tr>-->
<!--            <tr ng-if="$ctrl.selectedPhysicalFlow.description">-->
<!--                <td>Description</td>-->
<!--                <td>-->
<!--                    <span ng-bind="$ctrl.selectedPhysicalFlow.description"></span>-->
<!--                </td>-->
<!--            </tr>-->
<!--            </tbody>-->
<!--        </table>-->

<!--        <div class="small">-->
<!--            <waltz-last-updated entity="$ctrl.selectedPhysicalFlow"></waltz-last-updated>-->
<!--        </div>-->
<!--    </div>-->