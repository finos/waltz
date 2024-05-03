import {mkEntityLabelFormatter} from "../../../common/slick-grid-utils";
import {cmp, propCmp} from "../../../common/sort-utils";
import _ from "lodash";


function mkAssessmentColumns(defs) {
    return _.map(defs, d => {
        return {
            id: "assessment_definition/" + d.id,
            assessmentDefinitionId: d.id,
            name: d.name,
            sortable: false,
            width: 150,
            formatter: (row, cell, value, colDef, dataCtx) => {
                const assessmentRatingsForDef = dataCtx.assessmentRatingsByDefinitionId[colDef.assessmentDefinitionId] || [];
                return _
                    .chain(assessmentRatingsForDef)
                    .map(r => `
                        <span title='${r.description}'
                              style="border-left: solid 2px ${r.color}; padding-left: 0.2em">
                            ${r.name}
                        </span>`)
                    .join(", ")
                    .value();
            },
            sortFn: (a, b) => cmp(
                _.size(_.get(a, ["assessmentRatingsByDefinitionId", d.id])),
                _.size(_.get(b, ["assessmentRatingsByDefinitionId", d.id])))
        }
    });
}


function mkColumns(rulesView) {

    const scopeCol = {
        id: "scope",
        name: "Scope",
        field: "vantagePointReference",
        sortable:  true,
        width: 170,
        formatter: mkEntityLabelFormatter(null, true),
        sortFn: (a, b) => propCmp(a, b, ["vantagePointReference", "name"])
    };

    const subjectCol = {
        id: "subject",
        name: "Subject",
        field: "subjectReference",
        sortable:  true,
        width: 170,
        formatter: mkEntityLabelFormatter(null, true),
        sortFn: (a, b) => propCmp(a, b, ["subjectReference", "name"])
    };

    const dataTypeCol = {
        id: "dataType",
        name: "Data Type",
        field: "dataType",
        sortable:  true,
        width: 170,
        formatter: mkEntityLabelFormatter(null, true, `<i class="fa fa-asterisk"></i> <em>All</em>`),
        sortFn: (a, b) => propCmp(a, b, ["dataType", "name"])
    };

    const classificationCol = {
        id: "classification",
        name: "Classification",
        field: "classification",
        sortable:  true,
        width: 170,
        formatter: (row, cell, value) => {
            return value
                ? `<span style='border-left: solid 2px ${value.color}; padding-left: 0.2em;'>
                        ${value.name}
                   </span>`
                : "";
        },
        sortFn: (a, b) => propCmp(a, b, ["classification", "name"])
    };

    const directionCol = {
        id: "direction",
        name: "Direction",
        field: "classification",
        sortable:  true,
        width: 100,
        formatter: (row, cell, value) => {
            switch (value.direction) {
                case "INBOUND":
                    return `<i class='fa fa-arrow-left'></i> Consumer`;
                case "OUTBOUND":
                    return `<i class='fa fa-arrow-right'></i> Producer`;
                default:
                    return "";
            }
        },
        sortFn: (a, b) => propCmp(a, b, ["classification", "direction"])
    };

    const assessmentCols = mkAssessmentColumns(rulesView.primaryAssessmentDefinitions);

    return _.concat(
        [
            directionCol,
            dataTypeCol,
            subjectCol,
            scopeCol,
            classificationCol
        ],
        assessmentCols);
}


export function mkGridData(rulesView) {
    if (_.isNil(rulesView)) return null;

    const classificationsById = _.keyBy(rulesView.flowClassifications, c => c.id);
    const dataTypesById = _.keyBy(rulesView.dataTypes, d => d.id);
    const ratingSchemeItemsById = _.keyBy(rulesView.ratingSchemeItems, r => r.id);
    const assessmentRatingsByRuleId = _.groupBy(rulesView.assessmentRatings, ar => ar.entityReference.id);

    const rows = _.map(rulesView.flowClassificationRules, r => {
        const assessmentRatingsByDefinitionId = _
            .chain(assessmentRatingsByRuleId[r.id])
            .reduce(
                (acc, ar) => {
                    const values = acc[ar.assessmentDefinitionId] || [];
                    acc[ar.assessmentDefinitionId] = _.concat(values, ratingSchemeItemsById[ar.ratingId]);
                    return acc;
                },
                {})
            .value();

        const dataType = dataTypesById[r.dataTypeId];
        const classification = classificationsById[r.classificationId];

        const enrichment = {
            classification,
            dataType,
            assessmentRatingsByDefinitionId
        };

        return Object.assign({}, r, enrichment);
    });

    return {
        columns: mkColumns(rulesView),
        rows
    };
}

