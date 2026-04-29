<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
<#assign controllerConfig = jakartaEe.controller>
package ${packageController};

<#list controllerConfig.wrapperImports as importLine>
import ${importLine};
</#list>
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

<#function toSqlName value>
    <#if !value?? || !value?has_content>
        <#return "">
    </#if>
    <#return value
        ?replace("([a-z0-9])([A-Z])", "$1_$2", "r")
        ?replace("[^A-Za-z0-9_]", "_", "r")
        ?lower_case>
</#function>
<#assign resourceName = toSqlName(entityName) + "s">

<#list controllerConfig.wrapperAnnotations as annotation>
${annotation}
</#list>
@Path("/${resourceName}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ${entityName}Controller extends ${entityName}ControllerBase {
}
