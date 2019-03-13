package orm.manager;

import java.sql.SQLException;

public interface DbContext<E> {
    boolean persist(E entity) throws IllegalAccessException, SQLException;

    Iterable<E> find();

    Iterable<E> find(String where);

    E findFirst();

    E findFirst(String where);

    void delete(E entity);
}
