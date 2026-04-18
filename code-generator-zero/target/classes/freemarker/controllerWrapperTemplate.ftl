<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
package ${packageController};

import ${packageService}.${entityName}ServiceBase;

public class ${entityName}Controller extends ${entityName}ControllerBase {

    public ${entityName}Controller(${entityName}ServiceBase ${entityName?uncap_first}Service) {
        super(${entityName?uncap_first}Service);
    }
}
