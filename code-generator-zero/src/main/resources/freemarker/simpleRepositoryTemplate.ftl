<#ftl output_format="plainText">
package ${packageRepository};

import java.util.List;

import javax.persistence.EntityManager;

public interface SimpleRepository<T> {

    EntityManager getEntityManager();

    void setEntityManager(EntityManager em);

    T save(T entity);

    boolean update(T entity);

    List<T> findAll();

    T findById(Long id);

    void delete(T t);

    void deleteAll();

    int count();

    boolean existingById(Long id);
}
