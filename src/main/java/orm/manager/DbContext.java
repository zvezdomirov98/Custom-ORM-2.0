package orm.manager;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface DbContext<E> {
    boolean persist(E entity) throws IllegalAccessException, SQLException, InstantiationException, NoSuchMethodException, InvocationTargetException;

    Iterable<E> find() throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException;

    Iterable<E> find(String where) throws SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;

    E findFirst() throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException;

    E findFirst(String where) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException;

    void delete(E entity);
}
