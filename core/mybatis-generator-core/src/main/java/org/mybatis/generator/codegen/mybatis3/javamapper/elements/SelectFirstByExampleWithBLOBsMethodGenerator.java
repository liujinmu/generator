/*
 *    Copyright 2006-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

import java.util.Set;
import java.util.TreeSet;

public class SelectFirstByExampleWithBLOBsMethodGenerator  extends AbstractJavaMapperMethodGenerator {

    public SelectFirstByExampleWithBLOBsMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(type);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

        FullyQualifiedJavaType recordType = introspectedTable.getRules().calculateAllFieldsClass();
        importedTypes.add(recordType);

        Method method = new Method("selectFirstByExampleWithBLOBs");
        method.setDefault(true);

        method.setReturnType(recordType);
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$
        method.addBodyLine(String.format("List<%s> items = selectByExampleWithBLOBs(example);", recordType.getShortName()));
        method.addBodyLine("if (items == null || items.size() < 1) return null;");
        method.addBodyLine("return items.get(0);");

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        addMapperAnnotations(interfaze, method);

        if (context.getPlugins()
                .clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}
