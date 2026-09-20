CREATE OR REPLACE VIEW checklist_assignments_view AS
SELECT
    id,
    mechanic_user_id,
    template_id,
    template_name,
    unit_number,
    operator_full_name,
    mechanic_full_name,
    mileage,
    next_service,
    time_in,
    time_out,
    date,
    status,
    CASE
        WHEN status <> 'APPROVED' THEN
            ROW_NUMBER() OVER (
                PARTITION BY mechanic_user_id
                ORDER BY id
            )
    END AS option_number
FROM checklist_assignments;

CREATE OR REPLACE VIEW assignment_sections_view AS
SELECT
    sections.id,
    sections.uuid,
    sections.section_id,
    sections.assignment_id,
    sections.name,
    sections.position AS option_number,
    sections.status
FROM
	assignment_sections sections
	INNER JOIN assignment_items items ON items.section_id = sections.id
	INNER JOIN assignment_responses responses ON responses.item_id = items.id
GROUP BY
    sections.id,
    sections.uuid,
    sections.section_id,
    sections.assignment_id,
    sections.name,
    sections.position,
    sections.status
;

CREATE OR REPLACE VIEW assignment_items_view AS
SELECT
    items.id,
    items.uuid,
    items.item_id,
    items.section_id,
    items.label,
    items.position AS option_number
FROM
	assignment_items items
;