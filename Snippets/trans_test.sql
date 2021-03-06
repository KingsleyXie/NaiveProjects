/********* Create Table With Test Data (BEGIN) *********/
DROP DATABASE IF EXISTS trans_test_db;
CREATE DATABASE trans_test_db;
USE trans_test_db;
CREATE TABLE demo (
	id INTEGER NOT NULL AUTO_INCREMENT,
	value INTEGER,
	PRIMARY KEY(id)
) ENGINE = InnoDB;
INSERT INTO demo(value) VALUES (1), (2), (3);
/********* Create Table With Test Data (END) *********/



/********* Create Test Users (BEGIN) *********/
CREATE USER 'session1'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON trans_test_db.* TO 'session1'@'localhost';

CREATE USER 'session2'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON trans_test_db.* TO 'session2'@'localhost';
/********* Create Test Users (END) *********/



/********* Test Preparation - On Both Sessions (BEGIN) *********/
USE trans_test_db;
SET SESSION innodb_lock_wait_timeout = 3;

-- It is strongly recommended to reset(DROP & RE-CREATE)
-- the database before starting a new test session
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; -- OR:
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED; -- OR:
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ; -- OR:
SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE;
/********* Test Preparation - On Both Sessions (END) *********/





/********* Transction Isolation Level Test Logic Code - A More Intuitive Version (BEGIN) *********/
/*             # SESSION 1                                           # SESSION 2                 */

-- Lost Update Test Logic:

START TRANSACTION;                                     START TRANSACTION;
SELECT value FROM demo WHERE id = 1;
UPDATE demo SET value = value + 1 WHERE id = 1;
                                                       UPDATE demo SET value = value + 1 WHERE id = 1;
                                                       COMMIT;
COMMIT;
SELECT value FROM demo WHERE id = 1;

-- Dirty Read Test Logic:
                                                                                                      
START TRANSACTION;                                     START TRANSACTION;
SELECT value FROM demo WHERE id = 2;
                                                       UPDATE demo SET value = 233 WHERE id = 2;
SELECT value FROM demo WHERE id = 2;
                                                       ROLLBACK;
SELECT value FROM demo WHERE id = 2;
COMMIT;

-- Non-Repeatable Read Test Logic:
                                                                                                      
START TRANSACTION;                                     START TRANSACTION;
SELECT value FROM demo WHERE id = 3;
                                                       UPDATE demo SET value = 233 WHERE id = 3;
                                                       COMMIT;
SELECT value FROM demo WHERE id = 3;
COMMIT;

-- Phantom Read Test Logic:
                                                                                                      
START TRANSACTION;                                     START TRANSACTION;
SELECT COUNT(*) FROM demo;
                                                       INSERT INTO demo(value) VALUES (4), (5), (6);
                                                       COMMIT;
SELECT COUNT(*) FROM demo;
COMMIT;

-- Phantom Read Extra Test Logic (For RR Isolation Level):
                                                                                                      
START TRANSACTION;                                     START TRANSACTION;
SELECT COUNT(*) FROM demo;
                                                       INSERT INTO demo(id, value) VALUES (233, 0);
                                                       COMMIT;
SELECT COUNT(*) FROM demo;
UPDATE demo SET value = 233 WHERE id = 233;
SELECT COUNT(*) FROM demo;
COMMIT;
/********* Transction Isolation Level Test Logic Code - A More Intuitive Version (END) *********/






-- ************************************************************************************
-- ************************************************************************************
-- ************************************************************************************
-- The following code are of the same effect as shown above,
-- while the typesetting below may not seem so straightforward to understand
-- ************************************************************************************



/********* Test For Lost Update (BEGIN) *********/
-- On Session 1 & Session 2:
START TRANSACTION;
-- On Session 1:
SELECT value FROM demo WHERE id = 1;
UPDATE demo SET value = value + 1 WHERE id = 1;
-- On Session 2:
UPDATE demo SET value = value + 1 WHERE id = 1;
-- On Session 1 & Session 2:
COMMIT;
-- On Session 1:
SELECT value FROM demo WHERE id = 1;
/********* Test For Lost Update (END) *********/



/********* Test For Dirty Read (BEGIN) *********/
-- On Session 1 & Session 2:
START TRANSACTION;
-- On Session 1:
SELECT value FROM demo WHERE id = 2;
-- On Session 2:
UPDATE demo SET value = 233 WHERE id = 2;
-- On Session 1:
SELECT value FROM demo WHERE id = 2;
-- On Session 2:
ROLLBACK;
-- On Session 1:
SELECT value FROM demo WHERE id = 2;
COMMIT;
/********* Test For Dirty Read (END) *********/



/********* Test For Non-Repeatable Read (BEGIN) *********/
-- On Session 1 & Session 2:
START TRANSACTION;
-- On Session 1:
SELECT value FROM demo WHERE id = 3;
-- On Session 2:
UPDATE demo SET value = 233 WHERE id = 3;
COMMIT;
-- On Session 1:
SELECT value FROM demo WHERE id = 3;
COMMIT;
/********* Test For Non-Repeatable Read (END) *********/



/********* Test For Phantom Read (BEGIN) *********/
-- On Session 1 & Session 2:
START TRANSACTION;
-- On Session 1:
SELECT COUNT(*) FROM demo;
-- On Session 2:
INSERT INTO demo(value) VALUES (4), (5), (6);
COMMIT;
-- On Session 1:
SELECT COUNT(*) FROM demo;
COMMIT;
/********* Test For Phantom Read (END) *********/



/********* Extra Test For Phantom Read Under RR Isolation Level (BEGIN) *********/
-- On Session 1 & Session 2:
START TRANSACTION;
-- On Session 1:
SELECT COUNT(*) FROM demo;
-- On Session 2:
INSERT INTO demo(id, value) VALUES (233, 0);
COMMIT;
-- On Session 1:
SELECT COUNT(*) FROM demo;
UPDATE demo SET value = 233 WHERE id = 233;
SELECT COUNT(*) FROM demo;
COMMIT;
/********* Extra Test For Phantom Read Under RR Isolation Level (END) *********/

-- ************************************************************************************
-- ************************************************************************************
-- ************************************************************************************
