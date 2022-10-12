
DROP TABLE REQUEST;
DROP TABLE Employee;

CREATE TABLE IF NOT EXISTS Employee (
	userid serial,
	username VARCHAR PRIMARY KEY,
	passhash NUMERIC NOT NULL,
	permissionlevel VARCHAR NOT NULL DEFAULT('Employee')
);

CREATE TABLE IF NOT EXISTS REQUEST (
	requestID serial PRIMARY KEY,
	username VARCHAR NOT NULL,
	description VARCHAR NOT NULL,
	amount NUMERIC NOT NULL,
	status VARCHAR NOT NULL DEFAULT('Pending'),
	CONSTRAINT fk_username
		FOREIGN KEY(username)
			REFERENCES Employee(username)
);

INSERT INTO Employee(username, passhash, PERMISSIONlevel) Values
	('Manny Ger', -259997032, 'Manager'),
	('fred', -1916318885, DEFAULT),
	('pamela', -1524582912, DEFAULT),
	('Marco', -7992220, DEFAULT);

INSERT INTO REQUEST(username, description, amount) VALUES
	('fred', 'Flight from NYC to Seattle', 350),
	('pamela', 'Overnight stay in Dallas', 150),
	('Marco', 'Bought the new pokemon game', 60),
	('Manny Ger', 'Lunch with CEO', 100);

SELECT * FROM employee;
SELECT * FROM Request;

SELECT request.username AS username, description, amount, status FROM Request JOIN EMPLOYEE ON (employee.username = request.username) WHERE permissionlevel = 'Employee';

UPDATE request SET status = 'Denied' WHERE requestID = 3;

SELECT * FROM request;
DELETE FROM employee WHERE userid > 4;

ALTER TABLE USERS RENAME TO EMPLOYEE;
ALTER TABLE REQUESTS RENAME TO REQUEST;
