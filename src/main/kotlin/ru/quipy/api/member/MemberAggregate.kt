package ru.quipy.api.member

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "aggregate-member")
class MemberAggregate : Aggregate
