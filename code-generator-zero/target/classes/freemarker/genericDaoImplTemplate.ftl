<#ftl output_format="plainText">
<#assign packageDao = packageDao!"jacopo.with.develop.dao">
package ${packageDao};

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.zaxxer.hikari.HikariDataSource;

public abstract class GenericDaoImpl<T, ID> implements GenericDao<T, ID> {

    protected final HikariDataSource dataSource;
    private final Class<T> domainClass;

    protected GenericDaoImpl(HikariDataSource dataSource, Class<T> domainClass) {
        this.dataSource = dataSource;
        this.domainClass = domainClass;
    }

    protected final Class<T> getDomainClass() {
        return this.domainClass;
    }

    protected abstract String getTableName();

    protected abstract String getIdColumn();

    protected abstract Set<String> getPersistentFields();

    protected abstract String getInsertSql();

    protected abstract String getUpdateSql();

    protected abstract T mapRow(ResultSet resultSet) throws SQLException;

    protected abstract void bindInsertParameters(PreparedStatement statement, T entity) throws SQLException;

    protected abstract void bindUpdateParameters(PreparedStatement statement, T entity) throws SQLException;

    protected abstract void setIdParameter(PreparedStatement statement, int parameterIndex, ID id) throws SQLException;

    protected abstract ID extractId(T entity);

    protected abstract void assignGeneratedId(T entity, ResultSet generatedKeys) throws SQLException;

    @Override
    public T findById(ID id) throws SQLException {
        this.requireId(id);

        String sql = "SELECT * FROM " + this.getTableName() + " WHERE " + this.getIdColumn() + " = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            this.setIdParameter(statement, 1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return this.mapRow(resultSet);
                }
                return null;
            }
        }
    }

    @Override
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + this.getTableName();

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<T> results = new ArrayList<T>();

            while (resultSet.next()) {
                results.add(this.mapRow(resultSet));
            }

            return results;
        }
    }

    @Override
    public T save(T entity) throws SQLException {
        this.requireEntity(entity);

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(this.getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {

            this.bindInsertParameters(statement, entity);
            int modifiedRows = statement.executeUpdate();

            if (modifiedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    this.assignGeneratedId(entity, generatedKeys);
                }
            }

            return entity;
        }
    }

    @Override
    public boolean update(T entity) throws SQLException {
        this.requireEntity(entity);

        ID id = this.extractId(entity);
        this.requireId(id);

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(this.getUpdateSql())) {

            this.bindUpdateParameters(statement, entity);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(ID id) throws SQLException {
        this.requireId(id);

        String sql = "DELETE FROM " + this.getTableName() + " WHERE " + this.getIdColumn() + " = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            this.setIdParameter(statement, 1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsById(ID id) throws SQLException {
        this.requireId(id);

        String sql = "SELECT 1 FROM " + this.getTableName() + " WHERE " + this.getIdColumn() + " = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            this.setIdParameter(statement, 1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM " + this.getTableName();

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + this.getTableName();

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0L;
        }
    }

    @Override
    public List<T> searchByField(String field, Object value) throws SQLException {
        if (field == null || field.isBlank() || value == null) {
            throw new IllegalArgumentException("Il campo e il valore devono essere valorizzati");
        }
        if (!this.getPersistentFields().contains(field)) {
            throw new IllegalArgumentException("Campo non consentito per " + this.domainClass.getSimpleName() + ": " + field);
        }

        String sql = "SELECT * FROM " + this.getTableName() + " WHERE " + field + " = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<T>();

                while (resultSet.next()) {
                    results.add(this.mapRow(resultSet));
                }

                return results;
            }
        }
    }

    private void requireEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entita passata deve essere valorizzata");
        }
    }

    private void requireId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("L'id passato deve essere valorizzato");
        }
    }
}
