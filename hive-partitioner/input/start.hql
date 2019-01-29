CREATE DATABASE IF NOT EXISTS Library;
USE Library;

CREATE TABLE IF NOT EXISTS Books
(
BibNum int,
Title String,
Author String,
ISBN String,
PublicationYear String,
Publisher String,
Subjects String,
ItemType String,
ItemCollection String,
FloatingItem String,
ItemLocation String,
ReportDate String,
ItemCount int
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE;