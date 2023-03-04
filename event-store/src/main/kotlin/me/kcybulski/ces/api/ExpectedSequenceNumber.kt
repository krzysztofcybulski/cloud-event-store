package me.kcybulski.ces.api

sealed interface ExpectedSequenceNumber {

    object AnySequenceNumber : ExpectedSequenceNumber
    data class SpecificSequenceNumber(val number: Long) : ExpectedSequenceNumber

}