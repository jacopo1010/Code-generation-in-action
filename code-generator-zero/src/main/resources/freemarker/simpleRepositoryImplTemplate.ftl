<#ftl output_format="plainText">
package ${packagePersistence};

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;

import com.zaxxer.hikari.HikariDataSource;

public abstract class SimpleRepositoryImpl<T> implements SimpleRepository<T> {

    private final HikariDataSource dataSource;

    protected SimpleRepositoryImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DSLContext getDsl() {
        return DSL.using(this.dataSource, org.jooq.SQLDialect.POSTGRES);
    }

    protected abstract Table<?> getTable();

    protected abstract T toModel(Record record);

    protected abstract void bindRecord(UpdatableRecord<?> record, T entity);

    protected abstract void assignId(T entity, Long id);

    protected abstract Field<Long> getIdField();

    @Override
    public List<T> findAll() {
        return this.getDsl()
                .selectFrom(this.getTable())
                .fetch()
                .map(this::toModel);
    }

    @Override
    public Optional<T> findById(Long id) {
        return this.getDsl()
                .selectFrom(this.getTable())
                .where(this.getIdField().eq(id))
                .fetchOptional()
                .map(this::toModel);
    }

    @Override
    public boolean existsById(Long id) {
        return this.getDsl().fetchExists(
                this.getDsl().selectOne()
                        .from(this.getTable())
                        .where(this.getIdField().eq(id)));
    }

    @Override
    public T save(T entity) {
        UpdatableRecord<?> record = (UpdatableRecord<?>) this.getDsl().newRecord(this.getTable());
        this.bindRecord(record, entity);
        record.store();
        this.assignId(entity, record.get(this.getIdField()));
        return entity;
    }

    @Override
    public boolean delete(Long id) {
        return this.getDsl()
                .deleteFrom(this.getTable())
                .where(this.getIdField().eq(id))
                .execute() > 0;
    }

    @Override
    public void deleteAll() {
        this.getDsl().deleteFrom(this.getTable()).execute();
    }

    @Override
    public long count() {
        return this.getDsl().fetchCount(this.getTable());
    }
}
