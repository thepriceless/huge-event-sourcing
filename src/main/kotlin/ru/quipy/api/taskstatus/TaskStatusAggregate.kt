package ru.quipy.api.taskstatus

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "aggregate-task-status")
class TaskStatusAggregate : Aggregate
