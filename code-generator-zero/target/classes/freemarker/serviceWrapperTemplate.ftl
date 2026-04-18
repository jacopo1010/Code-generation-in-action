<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
<#assign manyToOneFields = metaClass.fields?values?filter(field -> field.relation && field.relationType == "MANY_TO_ONE" && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content)>
<#assign relationTypesToImport = []>
<#list manyToOneFields as field>
    <#if !relationTypesToImport?seq_contains(field.javaType)>
        <#assign relationTypesToImport = relationTypesToImport + [field.javaType]>
    </#if>
</#list>
package ${packageService};

import ${packageRepository}.${entityName}RepositoryBase;
<#list relationTypesToImport as relationType>
import ${packageRepository}.${relationType}RepositoryBase;
</#list>

public class ${entityName}Service extends ${entityName}ServiceBase {

    public ${entityName}Service(${entityName}RepositoryBase repository<#list relationTypesToImport as relationType>, ${relationType}RepositoryBase ${relationType?uncap_first}Repository</#list>) {
        super(repository<#list relationTypesToImport as relationType>, ${relationType?uncap_first}Repository</#list>);
    }
}
