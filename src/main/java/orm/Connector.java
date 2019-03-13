package orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Connector {
    private String connectionString;
    private Properties properties;
    private Connection dbConnection;

    public Connector(String connectionString,
                     String username,
                     String password) {
        setConnectionString(connectionString);
        setProperties(username, password);
    }

    public String getConnectionString() {
        return connectionString;
    }

    private void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public void initDbConnection() throws SQLException {
        this.dbConnection = DriverManager
                .getConnection(
                        this.getConnectionString(),
                        this.getProperties());
    }

    private Properties getProperties() {
        return properties;
    }

    private void setProperties(String username,
                              String password) {
        this.properties = new Properties();
        this.properties.setProperty("user", username);
        this.properties.setProperty("password", password);
    }
}
