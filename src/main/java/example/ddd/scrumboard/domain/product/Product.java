package example.ddd.scrumboard.domain.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import example.ddd.scrumboard.domain.backlog.item.BacklogItem;
import example.ddd.scrumboard.domain.backlog.item.BacklogItemId;
import example.ddd.scrumboard.domain.shared.AggregateRoot;

public class Product extends AggregateRoot<ProductId> {

	private Set<ProductBacklogItem> backlogItems;

	public void plan(@NonNull BacklogItem backlogItem) {
		BacklogItemId backlogItemId = backlogItem.getId();
		if (containsBacklogItem(backlogItemId)) {
			throw new IllegalArgumentException("Could not plan backlog item, backlog item is already planned "
					+ backlogItemId);
		}

		ProductBacklogItem productBacklogItem = new ProductBacklogItem(backlogItemId, lastPosition());
		backlogItems.add(productBacklogItem);
	}

	public void reorder(@NonNull BacklogItemId backlogItemId, @NonNull Integer newPosition) {
		Optional<ProductBacklogItem> reorderedBacklogItem = findBacklogItem(backlogItemId);
		if (!reorderedBacklogItem.isPresent()) {
			throw new IllegalArgumentException("Could not reorder backlog item, backlog item is not planned "
					+ backlogItemId);
		}

		requirePositivePosition(newPosition);

		int oldPosition = reorderedBacklogItem.get().getPosition();
		if (oldPosition == newPosition) {
			return;
		}

		Ordering<ProductBacklogItem> byPosition = Ordering.natural().onResultOf(
				new Function<ProductBacklogItem, Integer>() {

					@Override
					public Integer apply(ProductBacklogItem input) {
						return input.getPosition();
					}
				});

		List<ProductBacklogItem> sortedBacklogItems = byPosition.sortedCopy(backlogItems);
		Collections.swap(sortedBacklogItems, oldPosition, newPosition);

		List<BacklogItemId> backlogItemIds = new ArrayList<>(sortedBacklogItems.size());
		for (int position = 0; position < sortedBacklogItems.size(); position++) {
			ProductBacklogItem backlogItem = sortedBacklogItems.get(position);
			backlogItem.setPosition(newPosition);
			backlogItemIds.add(backlogItem.getId());
		}

		publish(new ProductBacklogItemReordered(getId(), backlogItemIds));
	}

	Product(@NonNull ProductId id, @NonNull Set<ProductBacklogItem> backlogItems) {
		super(id);
		this.backlogItems = backlogItems;
	}

	private int lastPosition() {
		return backlogItems.size();
	}

	private Optional<ProductBacklogItem> findBacklogItem(BacklogItemId backlogItemId) {
		for (ProductBacklogItem backlogItem : backlogItems) {
			if (backlogItem.getId().equals(backlogItemId)) {
				return Optional.of(backlogItem);
			}
		}
		return Optional.absent();
	}

	private boolean containsBacklogItem(BacklogItemId backlogItemId) {
		return findBacklogItem(backlogItemId).isPresent();
	}

	private int requirePositivePosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("Position must be positive but was " + position);
		} else {
			return position;
		}
	}

}
