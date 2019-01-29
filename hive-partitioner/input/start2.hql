CREATE DATABASE IF NOT EXISTS Parking;
USE Parking;

CREATE TABLE IF NOT EXISTS ParkingCitations
(
TicketNumber String,
IssueDate String,
IssueTime int,
MeterId String,
MarkedTime String,
RPStatePlate String,
PlateExpiryDate int,
VIN String,
Make String,
BodyStyle String,
Color String,
Location String,
Route String,
Agency int,
ViolationCode String,
ViolationDesc String,
FineAmount int,
Latitude int,
Longitude int
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE;