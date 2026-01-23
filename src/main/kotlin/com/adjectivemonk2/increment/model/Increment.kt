package com.adjectivemonk2.increment.model

import kotlinx.serialization.Serializable

@Serializable
data class Increment(val key: String, val value: Long)
