import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class DataManager {

	private Connection connection;
	private Statement stmt;
	private String hostFileLocation;
	private String userFileLocation;
	private String dbName;
	private String tableName;
	private String partitionTableName;

	public DataManager(Connection conn) {
		try {
			this.connection = conn;
			stmt = connection.createStatement();
			hostFileLocation = "/tmp/root/hive.log";
			userFileLocation = "C:\\git\\hive-partitioner\\hive-partitioner\\hive.log";
			dbName = "Parking";
			tableName = "ParkingCitations";
		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}

	public void Initialize(String createTableFile, String dataFile) {
		try {
			String[] queries = getQueries(createTableFile);
			for (int i = 0; i < queries.length; i++) {
				if (!queries[i].trim().equals("")) {
					stmt.executeUpdate(queries[i]);
					System.out.println(">> Executing query: " + queries[i]);
				}
			}
			System.out.println("Starting loading data into table " + tableName);
			System.out.println("Please wait...");
			stmt.executeUpdate("LOAD DATA LOCAL INPATH '" + dataFile + "' INTO TABLE " + tableName);
			System.out.println("Data loaded into table " + tableName);
		} catch (Exception e) {
			System.out.println(">> Error: " + e.toString());
			System.out.println("-------------------------------------------");
			System.out.println(">> Error:");
			e.printStackTrace();
		}
	}

	public void RunExampleQueries(String queriesFile, int numOfQueries) {
		try {
			String[] queries = getQueries(queriesFile);
			stmt.execute(queries[0]);

			if (queries.length > 0) {
				System.out.println("Executing example queries. Please wait...");
				for (int i = 0; i < numOfQueries; i++) {
					int rand = ThreadLocalRandom.current().nextInt(1, queries.length);
					System.out.println(">> Executing query: " + queries[rand]);
					stmt.executeQuery(queries[rand]);
				}
				System.out.println("Query execution finished.");
				this.acquireHiveLog();
			} else
				System.out.println("Queries file is empty.");
		} catch (Exception e) {
			System.out.println(">> Error: " + e.toString());
			System.out.println("-------------------------------------------");
			System.out.println(">> Error:");
			e.printStackTrace();
		}
	}

	public void TestPartitioning() {
		HashMap<String, Float> columns = this.getMostFreqColumns();

		String recommendedColumn = this.recommendPartitionColumn(columns);

		// Partition by recommended column
		long tEstimated = this.partitionTable(recommendedColumn);

		printEstimatedTime(tEstimated, recommendedColumn);
		
		// Partition by the rest of the columns
		columns.remove(recommendedColumn);
		for (Entry<String, Float> entry : columns.entrySet()) {
			String column = entry.getKey();

			tEstimated = this.partitionTable(column);
			printEstimatedTime(tEstimated, column);
		}
		
		// Partition by recommended and other, random column (multi-column partitioning)
		HashMap<String, String> multipleColumnsPartitioning = new HashMap<String, String>();
		
		String randColumn = randomColumn(columns);
		
		multipleColumnsPartitioning.put(recommendedColumn, "");
		multipleColumnsPartitioning.put(randColumn, "");
		
		tEstimated = this.partitionTable(multipleColumnsPartitioning);
		printEstimatedTime(tEstimated, recommendedColumn + " and " + randColumn);
		
		// Partition by any two columns 
		// (sans 'issuedate' and 'make', as we established that those columns are problematic for small clusters)
		columns.put(recommendedColumn, 0.0f); // return recommendedColumn to 'columns' HashMap for this testing
		columns.remove("issuedate");
		columns.remove("make");

		multipleColumnsPartitioning.clear();

		Iterator<String> columnIterator = columns.keySet().iterator();
		
		while (columnIterator.hasNext()) {
			String firstColumn = columnIterator.next();

			for (String secondColumn : columns.keySet()) {				
				if (!firstColumn.equalsIgnoreCase(secondColumn)) {

					multipleColumnsPartitioning.put(firstColumn, "");
					multipleColumnsPartitioning.put(secondColumn, "");
					
					tEstimated = this.partitionTable(multipleColumnsPartitioning);
					printEstimatedTime(tEstimated, firstColumn + " and " + secondColumn);
					
					multipleColumnsPartitioning.clear();
				}
			}
			
			columnIterator.remove();
		}
	}
	
	
	
	private long partitionTable(String partitionColumn) {
		HashMap<String, String> partitionColumns = new HashMap<String, String>();
		partitionColumns.put(partitionColumn, "");
		return this.partitionTable(partitionColumns);
	}

	private long partitionTable(HashMap<String, String> partitionColumns) {

		long tStart = 0;
		long tEstimated = 0;
		this.partitionTableName = this.tableName + "PartitionedBy" + String.join("And", partitionColumns.keySet());

		try {
			stmt.execute("SET hive.optimize.sort.dynamic.partition=true");
			stmt.execute("SET hive.exec.dynamic.partition=true");
			stmt.execute("SET hive.exec.dynamic.partition.mode=nonstrict");
			stmt.execute("SET hive.exec.max.dynamic.partitions=100000");
			stmt.execute("SET hive.exec.max.dynamic.partitions.pernode=100000");

			


			String partitionQuery = "CREATE TABLE IF NOT EXISTS " + dbName + "." + partitionTableName + " (";
			Vector<String> columns = new Vector<String>();
			Vector<String> columnsWithTypes = new Vector<String>();

			ResultSet result = stmt.executeQuery("DESCRIBE " + dbName + "." + tableName);

			while (result.next()) {
				String columnName = result.getString(1).toLowerCase();
				String columnType = result.getString(2);

				if (partitionColumns.keySet().contains(columnName)) {
					partitionColumns.put(columnName,columnType);
				} else {
					columnsWithTypes.add(columnName + " " + columnType);
					columns.add(columnName);
				}
			}

			columns.add(String.join(", ", partitionColumns.keySet()));
			
			ArrayList<String> partitionColumnsWithTypes = new ArrayList<String>();

			for (Entry<String, String> entry : partitionColumns.entrySet())
			{
				partitionColumnsWithTypes.add(entry.getKey() + " " + entry.getValue());
			}
			
			partitionQuery += String.join(",\r\n", columnsWithTypes) + ")\r\n";
			partitionQuery += "PARTITIONED BY(" + String.join(", ", partitionColumnsWithTypes) + ")\r\n"
					+ "ROW FORMAT DELIMITED\r\n" + "FIELDS TERMINATED BY '\\t'\r\n" + "STORED AS TEXTFILE";

			String copyToPartitionedTableQuery = "INSERT OVERWRITE TABLE " + dbName + "." + this.partitionTableName
					+ " PARTITION(" + String.join(", ", partitionColumns.keySet()) + ")" + " SELECT ";
			copyToPartitionedTableQuery += String.join(",\r\n", columns);
			copyToPartitionedTableQuery += " FROM " + dbName + "." + tableName;

			tStart = System.currentTimeMillis();
			System.out.println(">> Executing query: " + partitionQuery);
			stmt.execute(partitionQuery);

			System.out.println(">> Executing query: " + copyToPartitionedTableQuery);
			stmt.execute(copyToPartitionedTableQuery);
			tEstimated = System.currentTimeMillis() - tStart;

		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return tEstimated;
	}

	private String recommendPartitionColumn(HashMap<String, Float> columns) {

		Iterator<Entry<String, Float>> it = columns.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Float> column = it.next();
			String columnName = (String) column.getKey();
			int distVals;
			distVals = this.getNumDistinct(columnName);
			System.out.println("Column: " + columnName + " has " + distVals + " distinct values.");
			columns.put(columnName, columns.get(columnName) / distVals);
		}

		Map.Entry<String, Float> maxColumn = null;

		for (Map.Entry<String, Float> column : columns.entrySet()) {
			if (maxColumn == null || column.getValue().compareTo(maxColumn.getValue()) > 0) {
				maxColumn = column;
			}
		}
		return maxColumn.getKey();
	}

	private String[] getQueries(String fileName) throws Exception {

		String inputString = new String();
		StringBuffer inputStringBuff = new StringBuffer();
		String[] queries = {};
		try {

			FileReader fr = new FileReader(new File(fileName));
			BufferedReader br = new BufferedReader(fr);

			while ((inputString = br.readLine()) != null) {
				inputStringBuff.append(inputString + " ");
			}
			br.close();

			queries = inputStringBuff.toString().substring(0, inputStringBuff.length() - 1).split(";");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return queries;
	}

	private void acquireHiveLog() {
		try {
			JSch jsch = new JSch();
			jsch.addIdentity("C:/git/hive-partitioner/id_rsa.pem");

			String host = "root@127.0.0.1";
			String user = host.substring(0, host.indexOf('@'));
			host = host.substring(host.indexOf('@') + 1);

			Session session = jsch.getSession(user, host, 2222);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			Channel channel = session.openChannel("sftp");

			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);

			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.get(hostFileLocation, userFileLocation);
			sftpChannel.exit();
			session.disconnect();
			System.out.println("hive.log file stored in " + userFileLocation);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private HashMap<String, Float> getMostFreqColumns() {

		HashMap<String, Float> columns = this.getColumnsName();
		String columnName;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.userFileLocation));
			String line = reader.readLine();
			while (line != null) {
				if (Pattern.matches(".*Executing command.*SELECT.*WHERE.*", line)) {
					String afterWhere = line.substring(line.indexOf("WHERE"), line.length());
					String clause = getWhereClause(afterWhere).toLowerCase();
					Iterator<Entry<String, Float>> it = columns.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, Float> column = it.next();
						columnName = (String) column.getKey();
						if (clause.contains(columnName)) {
							columns.put(columnName, columns.get(columnName) + 1);
						}
					}
				}

				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		Iterator<Entry<String, Float>> it = columns.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Float> column = it.next();
			columnName = (String) column.getKey();
			if (columns.get(columnName) == 0) {
				it.remove();
			}
		}
		return columns;
	}

	private String getWhereClause(String queryLine) {
		String whereClause = queryLine;
		queryLine.toUpperCase();

		whereClause = queryLine.split("GROUP BY")[0].split("HAVING")[0].split("DISTRIBUTE BY")[0].split("CLUSTER BY")[0]
				.split("LIMIT")[0];

		return whereClause;
	}

	private HashMap<String, Float> getColumnsName() {
		HashMap<String, Float> columns = new HashMap<String, Float>();
		try {
			ResultSet result = stmt.executeQuery("DESCRIBE " + this.dbName + "." + this.tableName);
			while (result.next()) {
				String columnName = result.getString(1);
				columns.put(columnName, 0.0f);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return columns;
	}

	private int getNumDistinct(String columnName) {
		int numOfDistinctValues = 0;
		try {
			ResultSet result = stmt.executeQuery(
					"Select count(distinct " + columnName + ") from " + this.dbName + "." + this.tableName);
			result.next();
			numOfDistinctValues = Integer.parseInt(result.getString(1));
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return numOfDistinctValues;
	}

	private String randomColumn(HashMap<String, Float> columns) {
		Random rand = new Random();
		
		int idx = rand.nextInt(columns.size());
		
		return (String) columns.keySet().toArray()[idx];
	}

	private void printEstimatedTime(long time, String byColumn) {
		String estimatedTime = String.format("%02d min, %02d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(time),
			    TimeUnit.MILLISECONDS.toSeconds(time) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
			);
		
		System.out.println("Estimated time for partitioning by " + byColumn + " is: " + estimatedTime);
	}
	
}