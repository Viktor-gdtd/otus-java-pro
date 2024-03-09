package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl<T> implements EntitySQLMetaData {
    private final EntityClassMetaData<T> entityClassMetaData;

    public EntitySQLMetaDataImpl(EntityClassMetaData<T> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
    }

    @Override
    public String getSelectAllSql() {
        return "select " + allFieldNames() +
                "from " + tableName();
    }

    @Override
    public String getSelectByIdSql() {
        return "select " + allFieldNames() +
                "from " + tableName() +
                "where " + id() + "= (?)";
    }

    @Override
    public String getInsertSql() {
        return "insert into " +
                tableName() + "(" + fieldNamesWithoutId() + ") " +
                "values( " + values() + ")";
    }

    @Override
    public String getUpdateSql() {
        return "update " + tableName() +
                "set " + set() +
                "where " + id() + " = (?)";
    }

    private String tableName() {
        return entityClassMetaData.getName().concat(" ");
    }

    private String id() {
        return entityClassMetaData.getIdField().getName().concat(" ");
    }

    private String allFieldNames() {
        return entityClassMetaData.getAllFields().stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "))
                .concat(" ");
    }

    private String fieldNamesWithoutId() {
        return entityClassMetaData.getFieldsWithoutId().stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "))
                .concat(" ");
    }

    private String values() {
        return entityClassMetaData.getFieldsWithoutId().stream()
                .map(field -> "(?)")
                .collect(Collectors.joining(", "))
                .concat(" ");
    }

    private String set() {
        return entityClassMetaData.getAllFields().stream()
                .map(field -> field.getName() + " = (?)")
                .collect(Collectors.joining(", "))
                .concat(" ");
    }
}
