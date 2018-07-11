package org.sapzil.protobuf

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos

class CodeGenerator(private val request: PluginProtos.CodeGeneratorRequest) {
    private val response = PluginProtos.CodeGeneratorResponse.newBuilder()

    fun generate(): PluginProtos.CodeGeneratorResponse {
        val files = request.fileToGenerateList.asByteStringList().map { it.toStringUtf8() }.toSet()
        for (fileDesc in request.protoFileList) {
            if (fileDesc.name !in files) {
                continue
            }
            for (desc in fileDesc.messageTypeList) {
                handleMessage(fileDesc, desc, desc.name)
            }
        }
        return response.build()
    }

    private fun handleMessage(fileDesc: DescriptorProtos.FileDescriptorProto, desc: DescriptorProtos.DescriptorProto, name: String) {
        val pkg = fileDesc.options.javaPackage
        val dir = pkg.replace(".", "/")

//        val content = buildString {
//            append("package $pkg\n\n")
//            append("")
//        }
//        response.addFileBuilder()
//                .setName("$dir/${desc.name}.kt")
//                .setContent(content)
//                .build()

        // TODO: java_multiple_files, java_outer_class options
        val outerClassName = "${fileDesc.name[0].toUpperCase()}${fileDesc.name.substring(1).replace(".proto", "")}"
        val fullName = "$pkg.$outerClassName.$name"
        response.addFileBuilder()
                .setName("$dir/$outerClassName.java")
                .setInsertionPoint("class_scope:${fileDesc.`package`}.$name")
                .setContent("public static final org.sapzil.protobuf.KotlinMessage<$fullName, $fullName.Builder> kt = new org.sapzil.protobuf.KotlinMessage<>(new Builder());")
                .build()

        for (nestedDesc in desc.nestedTypeList) {
            handleMessage(fileDesc, nestedDesc, "${desc.name}.${nestedDesc.name}")
        }
    }
}

fun main(args: Array<String>) {
    val request = PluginProtos.CodeGeneratorRequest.parseFrom(System.`in`)
    val response = PluginProtos.CodeGeneratorResponse.newBuilder()
    val generator = CodeGenerator(request)
    generator.generate().writeTo(System.out)
    response.build().writeTo(System.out)
}
