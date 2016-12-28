IF OBJECT_ID('dbo.people', 'U') IS NOT NULL 
  DROP TABLE dbo.people;

CREATE TABLE people  (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);