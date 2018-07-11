package org.sapzil.protobuf

import com.google.protobuf.Message

class KotlinMessage<T : Message, Builder : Message.Builder>(private val builder: Builder) {
    private fun newBuilder(): Builder {
        return builder.defaultInstanceForType.newBuilderForType() as Builder
    }

    operator fun invoke(block: Builder.() -> Unit): T {
        return newBuilder().apply(block).build() as T
    }
}
