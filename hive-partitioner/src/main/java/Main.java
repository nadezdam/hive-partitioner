import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Main {
	private static String driver = "org.apache.hive.jdbc.HiveDriver";

	public static void main(String[] args) throws SQLException {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			Connection connect = DriverManager.getConnection("jdbc:hive2://localhost:10000", "", "");

			DataManager dm = new DataManager(connect);
			String createTableFile = args[0];
			String dataFile = args[1];
			dm.Initialize(createTableFile, dataFile);
			
			String queriesFile = args[2];
			int numOfQueries = 50;
			dm.RunExampleQueries(queriesFile, numOfQueries);
			
		    dm.TestPartitioning();
		    
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
