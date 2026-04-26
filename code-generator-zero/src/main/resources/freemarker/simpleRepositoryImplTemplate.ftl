<#ftl output_format="plainText">
package ${packageRepository};

import java.util.List;

import javax.persistence.EntityManager;

public class SimpleRepositoryImpl<T> implements SimpleRepository<T> {

    private EntityManager em;

    private Class<T> domainClass;

    public SimpleRepositoryImpl(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public T save(T entity) {
        T persistEntity = entity;
        this.em.persist(persistEntity);
        return persistEntity;
    }

    @Override
    public boolean update(T entity) {
        if (entity == null) {
            return false;
        }
        this.em.merge(entity);
        return true;
    }

    @Override
    public List<T> findAll() {
        return this.em.createQuery("select o from " + this.domainClass.getName() + " o", this.domainClass).getResultList();
    }

    @Override
    public T findById(Long id) {
        return this.em.find(this.domainClass, id);
    }

    @Override
    public void delete(T t) {
        this.em.remove(t);
    }

    @Override
    public void deleteAll() {
        this.em.createQuery("DELETE FROM" + this.domainClass.getName()).executeUpdate();
    }

    @Override
    public int count() {
        return (int)this.em.createQuery("SELECT COUNT(id) FROM" + this.domainClass.getName()).getSingleResult();
    }

    @Override
    public boolean existingById(Long id) {
        return this.findById(id) != null;
    }
}
