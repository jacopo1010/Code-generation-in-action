<#ftl output_format="plainText">
<#assign packageDao = packageDao!"jacopo.with.develop.dao">
package ${packageDao};

import java.sql.SQLException;
import java.util.List;

public interface GenericDao<T, ID> {

    T findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    T save(T entity) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(ID id) throws SQLException;

    boolean existsById(ID id) throws SQLException;

    void deleteAll() throws SQLException;

    long count() throws SQLException;

    List<T> searchByField(String field, Object value) throws SQLException;
}
