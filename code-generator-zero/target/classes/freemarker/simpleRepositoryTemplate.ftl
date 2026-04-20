<#ftl output_format="plainText">
package ${packageRepository};

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;

public interface SimpleRepository<T> {

    DSLContext getDsl();

    T save(T entity) throws Exception;

    boolean update(T entity) throws Exception;

    Optional<T> findById(Long id) throws Exception;

    List<T> findAll() throws Exception;

    boolean delete(Long id) throws Exception;

    void deleteAll() throws Exception;

    long count() throws Exception;

    boolean existsById(Long id) throws Exception;
}
