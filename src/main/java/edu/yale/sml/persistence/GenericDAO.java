package edu.yale.sml.persistence;

import java.util.List;

public interface GenericDAO<T> {
    public abstract void delete(T object) throws Throwable;

    Integer save(T object) throws Throwable;

    public abstract void delete(List<T> object) throws Throwable;

    List<T> findAll(Class classz) throws Throwable;

    List<T> findPagedResult(Class classz, int first, int last) throws Throwable;

    List<T> findPagedResult(Class classz, int first, int last, String orderClause) throws Throwable;

    List<T> findPagedResultByType(Class classz, int first, int last, String orderClause, String type, String field) throws Throwable;

    List<T> findPagedResultByType(Class classz, int first, int last, String orderClause, String type1, String field1, String type2, String field2) throws Throwable;

    int findByLevelCount(Class classz, String string, String field);

    int findByLevelCount(Class classz, String filterValue1, String field1, String filterValue2, String field2);

}