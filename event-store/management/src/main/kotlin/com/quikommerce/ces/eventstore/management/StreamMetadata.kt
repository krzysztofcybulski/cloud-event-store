package com.quikommerce.ces.eventstore.management

import org.litote.kmongo.id.StringId

data class StreamMetadata(
    val id: StringId<StreamMetadata>,
//    val type: String,
    val size: Int
)