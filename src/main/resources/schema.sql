drop table if exists CarModels;
drop table if exists CarManufacturers;

CREATE TABLE CarManufacturers(ID INT auto_increment PRIMARY KEY,
	NAME VARCHAR(255));

CREATE TABLE CarModels(
	ID INT auto_increment PRIMARY KEY,
	CarManufacturer int,
	NAME VARCHAR(255),
	TYPE VARCHAR(15),
	ReleaseTime TIMESTAMP(9) WITH TIME ZONE,
	foreign key (CarManufacturer) references CarManufacturers(id)
);

