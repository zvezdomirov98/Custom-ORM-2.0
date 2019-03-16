package orm.manager;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class EntityManager<E> implements DbContext<E> {
    private static final String SELECT_QUERY_TEMPLATE =
            "SELECT * FROM {0} ";
    private static final String UPDATE_QUERY_TEMPLATE =
            "UPDATE {0} {1} WHERE {2} = ''{3}''";
    private static final String SET_TEMPLATE =
            "SET {0} = ''{1}'' ";
    private static final String WHERE_LIMIT_TEMPLATE =
            "TRUE LIMIT 1;";

    private Class<E> klass;

    private Connection connection;

    public EntityManager(Class<E> klass,
                         Connection connection) {
        setKlass(klass);
        setConnection(connection);
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (!isEntityInDb(entity)) {
            doInsert(entity);
        } else {
            doUpdate(entity);
        }
        return false;
    }

    @Override
    public List<E> find()
            throws InvocationTargetException,
            SQLException,
            InstantiationException,
            IllegalAccessException,
            NoSuchMethodException {
        return find(null);
    }

    @Override
    public List<E> find(String where) throws SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String selectQuery;
        selectQuery = where == null ?
                MessageFormat.format(
                        SELECT_QUERY_TEMPLATE,
                        getTableName()) :
                MessageFormat.format(
                        SELECT_QUERY_TEMPLATE +
                                "WHERE " +
                                where,
                        getTableName(),
                        where
                );
        ResultSet rs = connection
                .prepareStatement(selectQuery)
                .executeQuery();
        return toList(rs);
    }

    @Override
    public E findFirst() throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return find(WHERE_LIMIT_TEMPLATE).get(0);
    }

    @Override
    public E findFirst(String where) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return find(where + " LIMIT 1").get(0);
    }

    @Override
    public void delete(E entity) {

    }

    private List<E> toList(ResultSet rs) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<E> result = new ArrayList<>();
        while (rs.next()) {
            E currentEntity = createEntity(rs);
            result.add(currentEntity);
        }
        return result;
    }

    private E createEntity(ResultSet rs)
            throws IllegalAccessException,
            InstantiationException, NoSuchMethodException,
            InvocationTargetException, SQLException {
        E entity = klass.getDeclaredConstructor()
                .newInstance();
        List<Field> entityFields = getAllColumnFields();
        int columnIndex = 1;
        Object currentValue;
        for (Field field : entityFields) {
            field.setAccessible(true);
            currentValue = getTypedValue(
                    field, rs, columnIndex++);
            field.set(entity, currentValue);
        }
        return entity;
    }

    private Object getTypedValue(Field field,
                                 ResultSet resultSet,
                                 int columnIndex) throws SQLException {
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            return resultSet.getString(columnIndex);
        } else if (fieldType == BigInteger.class ||
                fieldType == Long.class ||
                fieldType == long.class ||
                fieldType == Integer.class ||
                fieldType == int.class) {
            return resultSet.getLong(columnIndex);
        } else if (fieldType == Double.class ||
                fieldType == double.class ||
                fieldType == Float.class ||
                fieldType == float.class) {
            return resultSet.getDouble(columnIndex);
        } else if (fieldType == BigDecimal.class) {
            return resultSet.getBigDecimal(columnIndex);
        } else if (fieldType == Boolean.class) {
            return resultSet.getBoolean(columnIndex);
        } else if (fieldType == Date.class) {
            return resultSet.getDate(columnIndex);
        }
        return null;
    }

    private boolean isEntityInDb(E entity) throws IllegalAccessException, InvocationTargetException, SQLException, InstantiationException, NoSuchMethodException {
        Field primaryKeyField = getPrimaryKeyField();
        primaryKeyField.setAccessible(true);
        Object primaryKeyValue = primaryKeyField.get(entity);
        return findFirstById(primaryKeyField, primaryKeyValue) != null;
    }

    private boolean doInsert(E entity) throws SQLException {
        String values = String.join(
                ", ",
                getAllColumnValues(entity));
        String insertQuery = MessageFormat.format(
                getInsertQueryTemplate(entity),
                values
        );
        PreparedStatement preparedStatement = connection
                .prepareStatement(insertQuery,
                        Statement.RETURN_GENERATED_KEYS);
        try {
            connection.setAutoCommit(false);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) { //change to rowsAffected == 0 -> throw exception
                throw new SQLException(
                        "Creating entity failed, no rows affected."
                );
            }
            syncIdField(entity, preparedStatement);
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException | IllegalAccessException e) {
            connection.rollback();
            return false;
        }
    }

    private int doUpdate(E entity) throws IllegalAccessException, SQLException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        HashMap<Field, Object> fieldsToUpdate =
                getNotSyncedFields(entity);
        StringBuilder buildSetQuery = new StringBuilder();
        for (Map.Entry<Field, Object> entry : fieldsToUpdate.entrySet()) {
            buildSetQuery.append(MessageFormat.format(
                    SET_TEMPLATE,
                    entry.getKey().getName(),
                    entry.getValue().toString()
            ));
        }
        Field primaryKeyField = getPrimaryKeyField();
        primaryKeyField.setAccessible(true);

        String updateQuery = MessageFormat.format(
                UPDATE_QUERY_TEMPLATE,
                getTableName(),
                buildSetQuery.toString(),
                primaryKeyField.getName(),
                primaryKeyField.get(entity).toString()
        );
        return connection.prepareStatement(updateQuery)
                .executeUpdate();
    }

    private HashMap<Field, Object> getNotSyncedFields(E modelEntity) throws IllegalAccessException, InvocationTargetException, SQLException, InstantiationException, NoSuchMethodException {
        HashMap<Field, Object> result = new HashMap<>();
        List<Field> allEntityFields = getAllColumnFields();
        Field primaryKeyField = getPrimaryKeyField();
        primaryKeyField.setAccessible(true);
        E dbEntity = findFirstById(primaryKeyField,
                primaryKeyField.get(modelEntity));
        Field currentKey;
        Object currentValue;
        for (Field field : allEntityFields) {
            field.setAccessible(true);
            currentKey = field;
            currentValue = field.get(modelEntity);
            if (!currentValue.equals(field.get(dbEntity))) {
                result.put(currentKey, currentValue);
            }
        }
        return result;
    }

    private void syncIdField(E entity,
                             PreparedStatement preparedStatement)
            throws IllegalAccessException, SQLException {

        Field primaryKeyField = getPrimaryKeyField();
        primaryKeyField.setAccessible(true);
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            Long value = generatedKeys.getLong(1);
            primaryKeyField.set(entity, value);
        }
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

    private String parseToSqlArgument(Object objectValue) throws NullPointerException {
        if (objectValue instanceof Date) {
            objectValue = new SimpleDateFormat("yyyy-MM-dd").format(objectValue);
        }
        return "'" + objectValue.toString() + "'";
    }

    private E findFirstById(Field primaryKeyField,
                            Object primaryKeyValue) throws SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (primaryKeyValue == null) {
            return null;
        }
        String where = MessageFormat.format(
                "{0} = {1}",
                getColumnName(primaryKeyField),
                primaryKeyValue.toString()
        );
        return findFirst(where);
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

    private String getInsertQueryTemplate(E entity) {

        List<Field> nonNullColumnFields = getNonNullColumnFields(entity);
        String columnParameters =
                String.join(
                        ", ",
                        getColumnNames(nonNullColumnFields));
        return String.format(
                "INSERT INTO %s (%s) VALUES ({0})",
                getTableName(),
                columnParameters
        );
    }

    private List<String> getColumnNames(List<Field> columnFields) {
        return columnFields.stream()
                .map(this::getColumnName)
                .collect(Collectors.toList());
    }

    private String getColumnName(Field field) {
        return field.getAnnotation(Column.class).name();
    }

    private List<Field> getNonNullColumnFields(E entity) {
        return getAllColumnFields().stream()
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(entity) != null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private List<Field> getAllColumnFields() {
        return Arrays.stream(klass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .collect(Collectors.toList());
    }

    private String getTableName() {
        return klass.getAnnotation(Entity.class).name();
    }
}
