package volter.com.example.appscrumteam;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseHandler {

    Connection connect() throws SQLException;
    void close() throws SQLException;

    }

