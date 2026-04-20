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

import ${packageRepository}.${entityName}Repository;
<#list relationTypesToImport as relationType>
import ${packageRepository}.${relationType}Repository;
</#list>

public class ${entityName}Service extends ${entityName}ServiceBase {

    public ${entityName}Service(${entityName}Repository repository<#list relationTypesToImport as relationType>, ${relationType}Repository ${relationType?uncap_first}Repository</#list>) {
        super(repository<#list relationTypesToImport as relationType>, ${relationType?uncap_first}Repository</#list>);
    }
}
