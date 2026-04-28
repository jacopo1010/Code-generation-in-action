<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
<#assign serviceConfig = jakartaEe.service>
package ${packageService};

<#list serviceConfig.wrapperImports as importLine>
import ${importLine};
</#list>

<#list serviceConfig.wrapperAnnotations as annotation>
${annotation}
</#list>
public class ${entityName}Service extends ${entityName}ServiceBase {
}
