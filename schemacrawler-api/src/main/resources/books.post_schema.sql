-- Triggers
CREATE TRIGGER TRG_Authors BEFORE DELETE ON Authors FOR EACH ROW CALL "org.hsqldb.sample.TriggerSample";

-- Synonyms?
-- CREATE ALIAS ABS FOR "java.lang.Math.abs"

CREATE DOMAIN VALUE_STRING AS VARCHAR(20) 
  DEFAULT 'NO VALUE' 
  CHECK (VALUE IS NOT NULL AND CHARACTER_LENGTH(VALUE) > 2);