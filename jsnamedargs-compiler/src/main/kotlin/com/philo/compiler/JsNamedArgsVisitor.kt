package com.philo.compiler

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class JsNamedArgsVisitor(
    private val codeGenerator: CodeGenerator
) : KSVisitorVoid() {
    companion object {
        private val JsExport = ClassName("kotlin.js", "JsExport")
        private val ExperimentalJsExport = ClassName("kotlin.js", "ExperimentalJsExport")
        private val SuppressInlineAnnotation = AnnotationSpec.builder(Suppress::class).addMember(
            "%S",
            "NOTHING_TO_INLINE"
        ).build()
        private val SuppressDeprecationAnnotation = AnnotationSpec.builder(Suppress::class).addMember(
            "%S,%S",
            "DEPRECATION",
            "TYPEALIAS_EXPANSION_DEPRECATION"
        ).build()
    }
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        when (function.functionKind) {
            FunctionKind.TOP_LEVEL -> processTopLevelFunction(function)
            FunctionKind.MEMBER -> processMemberFunction(function, function.typeParameters)
            FunctionKind.STATIC -> TODO()
            FunctionKind.ANONYMOUS -> TODO()
            FunctionKind.LAMBDA -> TODO()
        }
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind != ClassKind.CLASS) return

        parseClassDeclaration(classDeclaration)
    }

    private fun parseClassDeclaration(classDeclaration: KSClassDeclaration) {
        val isInnerClass = classDeclaration.modifiers.contains(Modifier.INNER)

        classDeclaration.primaryConstructor?.let {
            processConstructorFunction(
                it,
                classDeclaration.typeParameters,
                if (isInnerClass) classDeclaration.toClassName().enclosingClassName() else null
            )
        }

        classDeclaration.getDeclaredFunctions().toList()
            .filter { it != classDeclaration.primaryConstructor }
            .forEach {
                processMemberFunction(it, classDeclaration.typeParameters)
            }

        classDeclaration.declarations.toList().forEach {
            if (it is KSClassDeclaration) {
                parseClassDeclaration(it)
            }
        }
    }

    private fun processTopLevelFunction(function: KSFunctionDeclaration) {
        if (function.functionKind != FunctionKind.TOP_LEVEL) return

        val functionName = function.simpleName.asString()
        val interfaceName = "${functionName.replaceFirstChar(Char::titlecase)}Args"
        val functionWithInterfaceName = "${functionName}Wrapper"

        processFunction(interfaceName, functionWithInterfaceName, null, function, function.typeParameters)
    }

    private fun processConstructorFunction(
        function: KSFunctionDeclaration,
        classTypeParameters: List<KSTypeParameter>,
        outerParentClassTypeName: TypeName? = null
    ) {
        if (!function.isConstructor()) return

        val constructorOf = function.returnType?.toTypeName(classTypeParameters.toTypeParameterResolver()) ?: return
        val constructorOfBaseName = constructorOf.toBaseTypeName()

        val interfaceName = "${constructorOfBaseName}ConstructorArgs"
        val functionWithInterfaceName = "create${constructorOfBaseName}Wrapper"

        processFunction(
            interfaceName,
            functionWithInterfaceName,
            null,
            function,
            classTypeParameters,
            outerParentClassTypeName
        )
    }

    private fun processMemberFunction(function: KSFunctionDeclaration, classTypeParameters: List<KSTypeParameter>) {
        if (function.functionKind != FunctionKind.MEMBER) return

        val parentClassDeclaration = function.closestClassDeclaration() ?: return

        val receiver = parentClassDeclaration.primaryConstructor?.run {
            returnType?.toTypeName(classTypeParameters.toTypeParameterResolver())
        } ?: return

        val functionName = function.simpleName.asString()
        val interfaceName = "${functionName.replaceFirstChar(Char::titlecase)}${receiver.toBaseTypeName()}Args"
        val functionWithInterfaceName = "${functionName}${receiver.toBaseTypeName()}Wrapper"

        processFunction(interfaceName, functionWithInterfaceName, receiver, function, classTypeParameters)
    }

    private fun processFunction(
        interfaceName: String,
        functionWithInterfaceName: String,
        receiver: TypeName?,
        function: KSFunctionDeclaration,
        typeParameters: List<KSTypeParameter>,
        outerParentClassTypeName: TypeName? = null
    ) {
        val packageName = function.packageName.asString()
        val parameters = function.parameters
        val returnTypeName = function.returnType?.toTypeName(typeParameters.toTypeParameterResolver())

        val functionGenericTypeNames = typeParameters.map { it.toTypeVariableName() }

        val receiverToUse = outerParentClassTypeName ?: receiver

        val functionCall: String = when {
            outerParentClassTypeName != null && returnTypeName != null -> returnTypeName.toBaseTypeName()
            function.isConstructor() && returnTypeName != null -> returnTypeName.toQualifiedBaseTypeName(packageName)
            else -> function.simpleName.asString()
        }

        if (parameters.isNotEmpty() && function.isPublic()) {
            val interfaceSpec = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(KModifier.EXTERNAL)
                .addAnnotation(ExperimentalJsExport)
                .addAnnotation(JsExport)
            val interfaceClass = ClassName(packageName, interfaceName)

            val functionWithInterfaceSpec = FunSpec.builder(functionWithInterfaceName)
                .addAnnotation(SuppressInlineAnnotation)
                .addAnnotation(ExperimentalJsExport)
                .addAnnotation(JsExport)
                .addModifiers(KModifier.INLINE)

            functionGenericTypeNames.forEach {
                interfaceSpec.addTypeVariable(it)
                functionWithInterfaceSpec.addTypeVariable(it)
            }

            receiverToUse?.let { functionWithInterfaceSpec.receiver(it) }

            if (functionGenericTypeNames.isNotEmpty()) {
                functionWithInterfaceSpec
                    .addParameter("args", interfaceClass.parameterizedBy(functionGenericTypeNames))
            } else {
                functionWithInterfaceSpec.addParameter("args", interfaceClass)
            }

            val funcStmtStringBuilder = StringBuilder()

            returnTypeName?.let {
                functionWithInterfaceSpec.returns(it)
                funcStmtStringBuilder.append("return ")
            }

            funcStmtStringBuilder.append("$functionCall(\n")

            parameters.forEach { param ->
                param.name?.asString()?.let { paramName ->
                    val paramType = param.type.toTypeName(typeParameters.toTypeParameterResolver())
                    interfaceSpec.addProperty(paramName, paramType)

                    funcStmtStringBuilder.append("\t$paramName = args.$paramName,\n")
                }
            }

            funcStmtStringBuilder.append(")")
            functionWithInterfaceSpec.addStatement(funcStmtStringBuilder.toString())

            val fileSpec = FileSpec.builder(packageName, interfaceName).apply {
                addType(interfaceSpec.build())
                addFunction(functionWithInterfaceSpec.build())
                addAnnotation(SuppressDeprecationAnnotation)
            }.build()

            fileSpec.writeTo(codeGenerator, false)
        }
    }
}

private fun TypeName.toBaseTypeName(): String {
    return this.toString().substringAfterLast(".").substringBefore("<")
}

private fun TypeName.toQualifiedBaseTypeName(packageName: String): String {
    return this.toString().substringAfterLast("$packageName.").substringBefore("<")
}
