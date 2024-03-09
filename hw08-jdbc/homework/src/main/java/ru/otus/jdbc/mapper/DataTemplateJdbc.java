package ru.otus.jdbc.mapper;

import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сохратяет объект в базу, читает объект из базы
 */
public class DataTemplateJdbc<T> implements DataTemplate<T> {

    private final DbExecutor dbExecutor;
    private final EntitySQLMetaData entitySQLMetaData;
    private final EntityClassMetaData<T> entityClassMetaData;
    private final Constructor<T> constructor;

    public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData entitySQLMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
        try {
            Field entityClassMetaData = entitySQLMetaData.getClass().getDeclaredField("entityClassMetaData");
            entityClassMetaData.setAccessible(true);
            this.entityClassMetaData = (EntityClassMetaData<T>) entityClassMetaData.get(entitySQLMetaData);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        this.constructor = entityClassMetaData.getConstructor();
        constructor.setAccessible(true);
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        String selectByIdSql = entitySQLMetaData.getSelectByIdSql();
        return dbExecutor.executeSelect(connection, selectByIdSql, List.of(id), this::getOne);
    }

    @Override
    public List<T> findAll(Connection connection) {
        String selectAllSql = entitySQLMetaData.getSelectAllSql();
        return dbExecutor.executeSelect(connection, selectAllSql, null, this::getMany)
                .orElseThrow(() -> new RuntimeException("Unexpected error"));
    }

    @Override
    public long insert(Connection connection, T client) {
        String insertSql = entitySQLMetaData.getInsertSql();
        List<Object> values = getValuesWithoutId(client);
        return dbExecutor.executeStatement(connection, insertSql, values);
    }

    @Override
    public void update(Connection connection, T client) {
        String updateSql = entitySQLMetaData.getUpdateSql();
        List<Object> valuesWithoutId = getValuesWithoutId(client);
        dbExecutor.executeStatement(connection, updateSql, valuesWithoutId);
    }

    private List<Object> getValuesWithoutId (T entity) {
        return entityClassMetaData.getFieldsWithoutId().stream()
                .map(field -> getValue(field, entity))
                .toList();
    }

    private Object getValue(Field field, T entity) {
        field.setAccessible(true);
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private T getOne(ResultSet resultSet) {
        return getMany(resultSet).get(0);
    }

    private List<T> getMany(ResultSet resultSet) {
        ArrayList<T> entities = new ArrayList<>();
        try {
            while (resultSet.next()) {
                T instance = constructor.newInstance();
                entityClassMetaData.getAllFields()
                        .forEach(field -> setFieldValue(instance, field, resultSet));
                entities.add(instance);
            }
        } catch (SQLException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return entities;
    }

    private void setFieldValue(T instance, Field field, ResultSet resultSet) {
        field.setAccessible(true);
        String fieldName = field.getName();
        try {
            field.set(instance, resultSet.getObject(fieldName));
        } catch (IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
