import entities.User;
import orm.Connector;
import orm.manager.EntityManager;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final String connectionString = "jdbc:mysql://localhost:3306/custom_orm_test";
    private static final String dbUsername = "root";
    private static final String dbPassword = "1234qwer";
    private static Connection dbConnection;

    public static void main(String[] args) throws SQLException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Connector connector = new Connector(
                connectionString,
                dbUsername,
                dbPassword);
        connector.initDbConnection();
        dbConnection = connector.getDbConnection();
        EntityManager<User> em = new EntityManager<>(User.class, dbConnection);
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(new User(
                    "pesho" + i,
                    "pass" + i,
                    new Date()));
        }
        for (User user : users) {
            em.persist(user);
        }
        users = em.find();
        users
                .forEach(System.out::println);

    }
}
