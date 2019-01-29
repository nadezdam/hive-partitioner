USE Parking;
SELECT TicketNumber
FROM ParkingCitations
WHERE IssueDate LIKE '_2015-09-15_';
SELECT TicketNumber
FROM ParkingCitations
WHERE Make='CHEV';
SELECT TicketNumber
FROM ParkingCitations
WHERE FineAmount=50;
SELECT TicketNumber
FROM ParkingCitations
WHERE Agency>54;
SELECT TicketNumber
FROM ParkingCitations
WHERE BodyStyle='_PA_';