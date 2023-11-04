package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import org.springframework.stereotype.Repository;


/**
* @description ${table.comment!} mapper 接口
* @date ${date}
* @author ${author}
*/
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>

public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

}
</#if>
