<#ftl output_format="plainText">
<#assign repositoryConfig = jakartaEe.repository>
package ${packageRepository};

import java.util.List;

import jakarta.persistence.EntityManager;
<#list repositoryConfig.entityManagerFieldImports as importLine>
import ${importLine};
</#list>

public class SimpleRepositoryImpl<T> implements SimpleRepository<T> {

<#list repositoryConfig.entityManagerFieldAnnotations as annotation>
    ${annotation}
</#list>
    private EntityManager em;

    private final Class<T> domainClass;

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
        return this.em.createQuery("select o from " + this.domainClass.getSimpleName() + " o", this.domainClass).getResultList();
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
        this.em.createQuery("DELETE FROM " + this.domainClass.getSimpleName()).executeUpdate();
    }

    @Override
    public int count() {
        return ((Long) this.em.createQuery("SELECT COUNT(o) FROM " + this.domainClass.getSimpleName() + " o")
                .getSingleResult()).intValue();
    }

    @Override
    public boolean existingById(Long id) {
        return this.findById(id) != null;
    }
}
