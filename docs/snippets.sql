-- This file contains useful SQL snippets.
-- To add a snippet locate the correct -- SECTION ---
-- or add a new one if none of the existing sections
-- are appropriate.



-- MEASURABLES ---

-- show all measurables (with ratings) for apps
SELECT app.name, mr.rating, m.name, m.measurable_kind
FROM measurable_rating mr
  INNER JOIN application app ON app.id = mr.entity_id
  INNER JOIN measurable m ON mr.measurable_id = m.id
;

-- select ratings of a specific kind
SELECT mr.*
FROM measurable_rating mr
  INNER JOIN measurable m ON mr.measurable_id = m.id
WHERE
  m.measurable_kind = 'PROCESS'
;
