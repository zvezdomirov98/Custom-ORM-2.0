import entities.User;
import orm.Connector;
import orm.manager.EntityManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

public class Main {
    private static final String connectionString = "jdbc:mysql://localhost:3306/custom_orm_test";
    private static final String dbUsername = "root";
    private static final String dbPassword = "1234qwer";
    private static Connection dbConnection;

    public static void main(String[] args) throws SQLException, IllegalAccessException {
        Connector connector = new Connector(
                connectionString,
                dbUsername,
                dbPassword);
        connector.initDbConnection();
        dbConnection = connector.getDbConnection();
        EntityManager<User> em = new EntityManager<>(User.class, dbConnection);

        User pesho = new User("pesho",
                "1234",
                new Date());
        em.persist(pesho);
    }
}
