package me.kcybulski.ces.eventstore.aggregate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import me.kcybulski.ces.eventstore.EventStoreConfiguration.eventStore
import me.kcybulski.ces.eventstore.aggregates.Aggregates
import me.kcybulski.ces.eventstore.aggregates.SaveAggregateResult.AggregateSaved

class AggregatesSpec : StringSpec({

    val aggregates = Aggregates.onEventStore(eventStore { })

    "should save mutable aggregate" {
        //given
        val aggregate = MutableOrderAggregate.createNew()
        aggregate.addProduct("Milk")

        //when
        val result = aggregates.save(aggregate)

        //then
        result.shouldBeInstanceOf<AggregateSaved<*>>()
    }

    "should load mutable aggregate" {
        //given
        val aggregate = MutableOrderAggregate.createNew()
        aggregate.addProduct("Milk")
        aggregates.save(aggregate)

        //when
        val loaded = aggregates
            .load<MutableOrderAggregate>(aggregate.stream)
            .shouldNotBeNull()

        //then
        loaded.products shouldHaveSingleElement "Milk"
    }

    "should save immutable aggregate" {
        //given
        val aggregate = ImmutableOrderAggregate.createNew()
        aggregate.addProduct("Milk")

        //when
        val result = aggregates.save(aggregate)

        //then
        result.shouldBeInstanceOf<AggregateSaved<*>>()
    }

    "should load immutable aggregate" {
        //given
        val aggregate = ImmutableOrderAggregate.createNew()
        aggregate.addProduct("Milk")
        aggregates.save(aggregate)

        //when
        val loaded = aggregates
            .load<ImmutableOrderAggregate>(aggregate.stream)
            .shouldNotBeNull()

        //then
        loaded.products shouldHaveSingleElement "Milk"
    }

})

