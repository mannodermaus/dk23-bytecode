package de.mannodermaus.dk23.ext

import kotlinx.metadata.isData
import kotlinx.metadata.jvm.KotlinClassMetadata

/**
 * Convenience function for determining whether or not a Metadata annotation describes a Kotlin `data class`.
 */
internal val Metadata.isDataClass: Boolean
    get() = try {
        val metadata = KotlinClassMetadata.read(this)
        metadata is KotlinClassMetadata.Class && metadata.kmClass.isData
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
