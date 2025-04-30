package volter.com.example.appscrumteam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLDatabaseHandler implements DatabaseHandler {

    private static final String url = "jdbc:mysql://localhost:3306/chatbotdb";
    private static final String user = "root";
    private static final String password = "Nc6xF76b!";

    private Connection connection;

    @Override
    public Connection connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
