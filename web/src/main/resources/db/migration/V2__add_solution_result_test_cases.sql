ALTER TABLE solution_result
    ADD COLUMN test_cases JSONB NOT NULL DEFAULT '[]'::jsonb;
