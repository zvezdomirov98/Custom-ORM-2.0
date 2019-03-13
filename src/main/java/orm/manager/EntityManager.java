package orm.manager;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class EntityManager<E> implements DbContext<E> {
    private static final String FIND_QUERY_TEMPLATE =
            "SELECT * FROM {0} ";
    private static final String WHERE_TEMPLATE =
            " WHERE {1} = {2} ";

    private Class<E> klass;

    private Connection connection;
    public EntityManager(Class<E> klass,
                         Connection connection) {
        setKlass(klass);
        setConnection(connection);
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException {
        if (!isEntityInDb(entity)) {
            doInsert(entity);
        } else {
            doUpdate(entity);
        }
        return false;
    }

    private boolean doInsert(E entity) throws SQLException {
        String values = String.join(
                ", ",
                getAllColumnValues(entity));
        String insertQuery = MessageFormat.format(
                getInsertQueryTemplate(),
                values
        );
        return this.connection
                .prepareStatement(insertQuery)
                .execute();
    }

    private List<String> getAllColumnValues(E entity) {
        return Arrays.stream(klass.getDeclaredFields())
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return parseToSqlArgument(field.get(entity));
                    } catch (IllegalAccessException | NullPointerException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String parseToSqlArgument(Object objectValue) throws NullPointerException{
        if (objectValue instanceof Date) {
            objectValue = new SimpleDateFormat("yyyy-MM-dd").format(objectValue);
        }
        return "'" + objectValue.toString() + "'";
    }

    private int doUpdate(E entity) {
        return 0;
    }

    private boolean isEntityInDb(E entity) throws IllegalAccessException {
        Field primaryKeyField = getPrimaryKeyField();
        primaryKeyField.setAccessible(true);
        Object primaryKeyValue = primaryKeyField.get(entity);
        return findFirstById(primaryKeyField, primaryKeyValue) != null;
    }

    private E findFirstById(Field primaryKeyField,
                            Object primaryKeyValue) {
        if(primaryKeyValue == null) {
            return null;
        }
        String where = MessageFormat.format(
                WHERE_TEMPLATE,
                getColumnName(primaryKeyField),
                primaryKeyValue.toString()
        );
        return findFirst(where);
    }

    private String getColumnName(Field field) {
        return field.getAnnotation(Column.class).name();
    }

    private Field getPrimaryKeyField() {
        return Arrays.stream(klass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Class " +
                                        klass.getSimpleName() +
                                        " has no primary key annotation present!"
                        )
                );
    }

    @Override
    public Iterable<E> find() {
        return null;
    }

    @Override
    public Iterable<E> find(String where) {
        return null;
    }

    @Override
    public E findFirst() {
        return null;
    }

    @Override
    public E findFirst(String where) {
        return null;
    }

    @Override
    public void delete(E entity) {

    }

    public Connection getConnection() {
        return connection;
    }

    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Class<E> getKlass() {
        return klass;
    }

    private void setKlass(Class<E> klass) {
        this.klass = klass;
    }

    /*TODO: Figure out how to set the columnParameters to match the non-null values.*/
    private String getInsertQueryTemplate() {
        String columnParameters =
                String.join(", ", getAllColumnNames());
        return String.format(
                "INSERT INTO %s (%s) VALUES ({0})",
                getTableName(),
                columnParameters
        );
    }

    //TODO: Check if it works without .filter
    private List<String> getAllColumnNames() {
        return Arrays.stream(klass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(field -> field.getAnnotation(Column.class).name())
                .collect(Collectors.toList());
    }

    private String getTableName() {
        return klass.getAnnotation(Entity.class).name();
    }
}
