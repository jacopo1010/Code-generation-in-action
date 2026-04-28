<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
<#assign controllerConfig = jakartaEe.controller>
package ${packageController};

<#list controllerConfig.wrapperImports as importLine>
import ${importLine};
</#list>

<#list controllerConfig.wrapperAnnotations as annotation>
${annotation}
</#list>
public class ${entityName}Controller extends ${entityName}ControllerBase {
}
