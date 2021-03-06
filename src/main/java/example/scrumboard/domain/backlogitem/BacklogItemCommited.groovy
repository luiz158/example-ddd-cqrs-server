package example.scrumboard.domain.backlogitem

import static java.util.Objects.requireNonNull
import example.ddd.Event
import example.scrumboard.domain.sprint.SprintId
import groovy.transform.Immutable
import groovy.transform.TypeChecked

@Immutable(knownImmutableClasses = [BacklogItemId.class, SprintId.class])
@TypeChecked
class BacklogItemCommited implements Event {
	BacklogItemId backlogItemId
	SprintId sprintId
}
