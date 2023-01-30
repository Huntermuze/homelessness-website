package app.customtypes.customcollections;

import java.util.*;

/**An efficient partially immutable Least Recently Used caching system that dynamically updates to prevent GC
 * pressure and mitigate amount of major gc invocations. Multiple instances of this object will exist.
 *
 * @author Matthew Yamen
 */

/* --REVISED (SEE NOTE)--
 * NOTE: Initially there was a HashSet and LinkedList as they were an efficient combo, time-complexity wise, for
 * the operations they were expected to perform. However, upon evaluation, a single LinkedHashMap was the better solution,
 * whilst slightly lesser time complexity it will not make an iota of difference due to the capped size of 10 items.
 * Thereby, worrying about performance is therefore not wise, and rather, space complexity should be the focus. In addition,
 * the implementation for a LinkedHashMap can be manipulated to create an LRU caching system as done below.*/

public final class LRUCache<K, V> extends LinkedHashMap<K, V>
{
	private final int capacity;

	public LRUCache(int capacity)
	{
		super(capacity, 0.75f, true);
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry (Map.Entry<K, V> eldest) { return size() > capacity; }

	public <T> RowDataList<T> generateNewList(RowDataList<T> list)
	{ return (list == null ? (new RowDataList<>()) : list); }
}
