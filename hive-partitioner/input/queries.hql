USE Library;
SELECT Title
FROM Books
WHERE PublicationYear LIKE '2017';
SELECT Title
FROM Books
WHERE ItemLocation='cen';
SELECT Title
FROM Books
WHERE ItemCount=2;