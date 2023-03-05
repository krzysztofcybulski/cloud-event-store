package me.kcybulski.ces.eventstore

sealed interface ExpectedSequenceNumber {

    object AnySequenceNumber : ExpectedSequenceNumber
    data class SpecificSequenceNumber(val number: Long) : ExpectedSequenceNumber

}