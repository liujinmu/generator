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
package org.mybatis.generator.codegen.mybatis3.javamanager;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamanager.elements.ManagerMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaManagerGenerator extends AbstractJavaClientGenerator {

    public JavaManagerGenerator(String project) {
        this(project, true);
    }

    public JavaManagerGenerator(String project, boolean requiresMatchedXMLGenerator) {
        super(project, requiresMatchedXMLGenerator);

    }

    public static FullyQualifiedJavaType getMapperFieldType(IntrospectedTable introspectedTable) {
        String mapperType = introspectedTable.getMyBatis3JavaSubMapperType();
        if (mapperType == null) {
            mapperType = introspectedTable.getMyBatis3JavaMapperType();
        }
        return new FullyQualifiedJavaType(mapperType);
    }

    public static String getMapperFieldName(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType fullMapperType = getMapperFieldType(introspectedTable);
        String mapperType = fullMapperType.getShortName();
        return mapperType.substring(0, 1).toLowerCase() + mapperType.substring(1);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedJavaType fullMapperType = getMapperFieldType(introspectedTable);
        String mapperFieldName = getMapperFieldName(introspectedTable);

        progressCallback.startTask(getString("Progress.17", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));

        FullyQualifiedJavaType recordType = introspectedTable.getRules().calculateAllFieldsClass();
        FullyQualifiedJavaType managerType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaManagerType());
        TopLevelClass interfaze = new TopLevelClass(managerType);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        interfaze.addImportedType(recordType);

        interfaze.addImportedType("org.springframework.stereotype.Component");
        interfaze.addImportedType("lombok.RequiredArgsConstructor");
        interfaze.addImportedType(fullMapperType);
        interfaze.addAnnotation("@Component");
        interfaze.addAnnotation("@RequiredArgsConstructor");

        Field mapperField = new Field(mapperFieldName, fullMapperType);
        mapperField.setFinal(true);
        mapperField.setVisibility(JavaVisibility.PRIVATE);
        interfaze.addField(mapperField);

        CommentGenerator commentGenerator = context.getCommentGenerator();
        commentGenerator.addJavaFileComment(interfaze);

        String rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (!stringHasValue(rootInterface)) {
            rootInterface = context.getJavaClientGeneratorConfiguration()
                    .getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }

        if (stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        }

        addBuilderMethod(interfaze);

        List<CompilationUnit> answer = new ArrayList<>();

        answer.add(interfaze);

        List<CompilationUnit> extraCompilationUnits = getExtraCompilationUnits();
        if (extraCompilationUnits != null) {
            answer.addAll(extraCompilationUnits);
        }

        return answer;
    }

    public List<CompilationUnit> getExtraCompilationUnits() {
        return Collections.emptyList();
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return new XMLMapperGenerator();
    }


    protected void addBuilderMethod(TopLevelClass interfaze) {
        ManagerMethodGenerator methodGenerator = new ManagerMethodGenerator();
        initializeAndExecuteGenerator(methodGenerator, interfaze);
    }

}
