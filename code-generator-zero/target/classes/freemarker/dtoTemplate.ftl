<#ftl output_format="plainText">
package ${packageDto!"jacopo.with.develop.dto"};

<#assign fields = metaClass.fields?values>
<#assign collectionFields = fields?filter(field -> field.collection)>
<#assign usesTimestamp = fields?filter(field -> field.javaType == "Timestamp")?size gt 0>
<#assign packageModel = packageModel!"jacopo.with.develop.model">
<#assign modelImports = []>
<#list fields as field>
    <#if metaClasses[field.javaType]?? && field.javaType != metaClass.name && !modelImports?seq_contains(field.javaType)>
        <#assign modelImports = modelImports + [field.javaType]>
    </#if>
</#list>

<#if usesTimestamp>
import java.sql.Timestamp;
</#if>
<#if collectionFields?size gt 0>
import java.util.List;
import java.util.ArrayList;
</#if>
<#list modelImports as modelImport>
import ${packageModel}.${modelImport};
</#list>

/**
 * DTO generato automaticamente per ${metaClass.name}.
 */
public class ${metaClass.name}Dto {
<#list fields as field>

    private <#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> ${field.name}<#if field.collection> = new ArrayList<>()</#if>;
</#list>

    public ${metaClass.name}Dto() {
    }
<#list fields as field>

    public <#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> get${field.name?cap_first}() {
        return this.${field.name};
    }

    public void set${field.name?cap_first}(<#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> ${field.name}) {
        this.${field.name} = ${field.name};
    }
</#list>
}
