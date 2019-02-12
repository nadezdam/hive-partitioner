### `ParkingCitations` table partitioning results:

| Partitioned by column(s) 	| Estimated time of partitioning 	| # of distinct values in column 	|
|:------------------------:	|:------------------------------:	|:------------------------------:	|
|        FineAmount        	|           3mins 2secs          	|               39               	|
|          Agency          	|          3mins 53secs          	|               44               	|
|         BodyStyle        	|          9mins 25secs          	|               194              	|
|         IssueDate        	|              ERROR             	|              1855              	|
|           Make           	|          35mins 40secs         	|              2150              	|
|                          	|                                	|                                	|
|    FineAmount + Agency   	|          10mins 10secs         	|        39 * 44 (approx.)       	|
|  FineAmount + BodyStyle  	|          16mins 26secs         	|       39 * 194 (approx.)       	|
|    Agency + BodyStyle    	|          12mins 27secs         	|       44 * 194 (approx.)       	|
|  FineAmount + IssueDate  	|              ERROR             	|       39 * 1855 (approx.)      	|
|     FineAmount + Make    	|              ERROR             	|       39 * 2150 (approx.)      	|