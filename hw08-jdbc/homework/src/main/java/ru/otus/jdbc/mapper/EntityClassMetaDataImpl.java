package ru.otus.jdbc.mapper;

import ru.otus.annotation.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {
    private Class<T> tClass;
    public EntityClassMetaDataImpl(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public String getName() {
        return tClass.getSimpleName();
    }

    @Override
    public Constructor<T> getConstructor() {
        return (Constructor<T>) Arrays.stream(tClass.getDeclaredConstructors())
                .min(Comparator.comparing(Constructor::getParameterCount))
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Field getIdField() {
        return getAllFields().stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public List<Field> getAllFields() {
        return Arrays.asList(tClass.getDeclaredFields());
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        return getAllFields().stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .toList();
    }
}
