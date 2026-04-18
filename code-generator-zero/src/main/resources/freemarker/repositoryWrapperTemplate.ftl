<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
package ${packageRepository};

import com.zaxxer.hikari.HikariDataSource;

public class ${entityName}Repository extends ${entityName}RepositoryBase {

    public ${entityName}Repository(HikariDataSource dataSource) {
        super(dataSource);
    }
}
