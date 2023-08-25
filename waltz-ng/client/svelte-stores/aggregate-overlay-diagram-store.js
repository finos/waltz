import {remote} from "./remote";


export function mkOverlayDiagramStore() {

    const getById = (id, force = false) => {
        return remote
            .fetchViewDatum(
                "GET",
                `api/aggregate-overlay-diagram/id/${id}`,
                {force});
    };


    const findAll = (force = false) => {
        return remote
            .fetchViewList(
                "GET",
                "api/aggregate-overlay-diagram/all",
                [],
                {force});
    };


    const findByKind = (kind, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram/diagram-kind/${kind}`,
                [],
                {force});
    };


    const findAppCountsForDiagram = (diagramId, widgetParameters, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/app-count-widget`,
                widgetParameters,
                {force});
    };

    const findTargetAppCostForDiagram = (diagramId, widgetParameters, futureDate, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/target-app-cost-widget`,
                widgetParameters,
                {force});
    };

    const findAppCostForDiagram = (diagramId, widgetParameters, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/app-cost-widget`,
                widgetParameters,
                {force});
    };

    const findAppAssessmentsForDiagram = (diagramId, widgetParameters, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/app-assessment-widget`,
                widgetParameters,
                {force});
    };

    const findComplexitiesForDiagram = (diagramId, widgetParameters, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/complexity-widget`,
                widgetParameters,
                {force});
    };

    const findAggregatedEntitiesForDiagram = (diagramId, widgetParameters, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/aggregated-entities-widget`,
                widgetParameters,
                {force});
    };

    const findAttestationsForDiagram = (diagramId, widgetParameters, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/attestation`,
                widgetParameters,
                {force});
    };

    const findBackingEntitiesForDiagram = (diagramId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/backing-entity-widget`,
                null,
                {force});
    };

    const findPresetsForDiagram = (diagramId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram/diagram-id/${diagramId}/presets`,
                null,
                {force});
    };

    const createPreset = (createCommand, force = false) => {
        return remote
            .execute(
                "POST",
                "api/aggregate-overlay-diagram/create-preset",
                createCommand,
                {force});
    };

    const save = (saveCommand) => {
        return remote
            .execute(
                "POST",
                "api/aggregate-overlay-diagram/save",
                saveCommand);
    };

    return {
        getById,
        findAll,
        findByKind,
        findAppCountsForDiagram,
        findTargetAppCostForDiagram,
        findAttestationsForDiagram,
        findAppCostForDiagram,
        findAppAssessmentsForDiagram,
        findBackingEntitiesForDiagram,
        findAggregatedEntitiesForDiagram,
        findComplexitiesForDiagram,
        findPresetsForDiagram,
        createPreset,
        save
    };
}


export const aggregateOverlayDiagramStore = mkOverlayDiagramStore();