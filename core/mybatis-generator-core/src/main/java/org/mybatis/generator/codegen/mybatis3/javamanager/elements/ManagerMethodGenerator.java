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
package org.mybatis.generator.codegen.mybatis3.javamanager.elements;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamanager.JavaManagerGenerator;

import java.util.Set;
import java.util.TreeSet;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getSetterMethodName;

public class ManagerMethodGenerator extends AbstractJavaMethodGenerator {

    public ManagerMethodGenerator() {
        super();
    }

    @Override
    public void addClassElements(TopLevelClass interfaze) {
        FullyQualifiedJavaType recordType = introspectedTable.getRules().calculateAllFieldsClass();
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType mapperType = JavaManagerGenerator.getMapperFieldType(introspectedTable);

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();

        importedTypes.add(recordType);
        importedTypes.add(mapperType);
        importedTypes.add(exampleType);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        interfaze.addImportedTypes(importedTypes);


        Method builderMethod = generateBuilderMethod(introspectedTable, recordType);
        Method insertMethod = generateInsertMethod(introspectedTable, recordType);
        Method selectByIdMethod = generateSelectByIdMethod(introspectedTable, recordType);
        Method selectByGuidMethod = generateSelectByGuidMethod(introspectedTable, recordType);

        addMethod(interfaze, builderMethod);
        addMethod(interfaze, insertMethod);
        addMethod(interfaze, selectByIdMethod);
        addMethod(interfaze, selectByGuidMethod);
    }


    private void addMethod(TopLevelClass interfaze, Method method) {
        if (method != null) {
            context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
            interfaze.addMethod(method);
        }
    }


    private static Method generateBuilderMethod(IntrospectedTable introspectedTable, FullyQualifiedJavaType recordType) {
        Method method = new Method("buildObject");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setStatic(true);
        method.setReturnType(recordType);

        introspectedTable.getBaseColumns().forEach(column -> {
            method.addParameter(new Parameter(column.getFullyQualifiedJavaType(), column.getJavaProperty()));
        });

        method.addBodyLine(String.format("%s obj = new %s();", recordType.getShortName(), recordType.getShortName()));

        introspectedTable.getBaseColumns().forEach(column -> {
            method.addBodyLine(String.format("obj.%s(%s);", getSetterMethodName(column.getJavaProperty()), column.getJavaProperty()));
        });

        method.addBodyLine("return obj;");
        return method;
    }


    private static Method generateInsertMethod(IntrospectedTable introspectedTable, FullyQualifiedJavaType recordType) {
        String mapperFieldName = JavaManagerGenerator.getMapperFieldName(introspectedTable);

        Method method = new Method("insert");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("int"));
        method.addParameter(new Parameter(recordType, "obj"));
        method.addBodyLine(String.format("return %s.insertSelective(obj);", mapperFieldName));
        return method;
    }

    private static Method generateSelectByIdMethod(IntrospectedTable introspectedTable, FullyQualifiedJavaType recordType) {
        String mapperFieldName = JavaManagerGenerator.getMapperFieldName(introspectedTable);

        Method method = new Method("selectById");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(recordType);
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "id"));
        method.addBodyLine(String.format("return %s.selectByPrimaryKey(id);", mapperFieldName));
        return method;
    }

    private static Method generateSelectByGuidMethod(IntrospectedTable introspectedTable, FullyQualifiedJavaType recordType) {
        boolean hasGuid = introspectedTable.getBaseColumns().stream()
                .anyMatch(column -> "guid".equals(column.getActualColumnName()));
        if (!hasGuid) {
            return null;
        }
        String mapperFieldName = JavaManagerGenerator.getMapperFieldName(introspectedTable);
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());

        Method method = new Method("selectByGuid");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(recordType);
        method.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "guid"));

        method.addBodyLine("if (guid == null || guid.length() == 0) return null;");
        method.addBodyLine("");
        method.addBodyLine(String.format("%s example = new %s();", exampleType.getShortName(), exampleType.getShortName()));
        method.addBodyLine("example.createCriteria().andGuidEqualTo(guid);");

        method.addBodyLine(String.format("return %s.selectFirstByExample(example);", mapperFieldName));
        return method;
    }
}
