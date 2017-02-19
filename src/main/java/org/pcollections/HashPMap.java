package org.pcollections;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;




/**
 * 
 * A persistent hash map.
 * <p>
 * This map uses a given integer map to map hashcodes to lists of elements
 * with the same hashcode. Thus if all elements have the same hashcode, performance
 * is reduced to that of an association list.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractMap and AbstractSet are thread-safe),
 * although its iterators may not be.
 * 
 * @author harold
 *
 * @param <K>
 * @param <V>
 */
public final class HashPMap<K,V> extends AbstractMap<K,V> implements PMap<K,V> {
//// STATIC FACTORY METHODS ////
	/**
	 * @param <K>
	 * @param <V>
	 * @param intMap
	 * @return a map backed by an empty version of intMap,
	 * 	i.e. backed by intMap.minusAll(intMap.keySet())
	 */
	@SuppressWarnings("unchecked")
	public static <K,V> HashPMap<K,V> empty(final PMap<Integer,PSequence<Entry<K,V>>> intMap) {
		return new HashPMap<K,V>((PMap) intMap.minusAll(intMap.keySet()), 0); }
	

//// PRIVATE CONSTRUCTORS ////
	private final PMap<Integer,ConsPStack<Entry<K,V>>> intMap;
	private final int size;
	// not externally instantiable (or subclassable):
	private HashPMap(final PMap<Integer,ConsPStack<Entry<K,V>>> intMap, final int size) {
		this.intMap = intMap; this.size = size; }

	
//// REQUIRED METHODS FROM AbstractMap ////
	// this cache variable is thread-safe since assignment in Java is atomic:
	private Set<Entry<K,V>> entrySet = null;
	@Override
	public Set<Entry<K,V>> entrySet() {
		if(entrySet==null)
			entrySet = new AbstractSet<Entry<K,V>>() {
				// REQUIRED METHODS OF AbstractSet // 
				@Override
				public int size() {
					return size; }		
				@Override
				public Iterator<Entry<K,V>> iterator() {
					return new SequenceIterator<Entry<K,V>>(intMap.values().iterator()); }
				// OVERRIDDEN METHODS OF AbstractSet //
				@Override
				public boolean contains(final Object e) {
					if(!(e instanceof Entry))
						return false;
					Entry<?,?> entry = (Entry<?, ?>) e;
					V value = get(entry.getKey());
					return value != null
							? value.equals(entry.getValue())
							: entry.getValue() == null && containsKey(entry.getKey());
				}
			};
		return entrySet;
	}

	
//// OVERRIDDEN METHODS FROM AbstractMap ////
	@Override
	public int size() {
		return size; }

	@Override
	public boolean containsKey(final Object key) {
		return keyIndexIn(getEntries(Objects.hashCode(key)), key) != -1; }
	
	@Override
	public V get(final Object key) {
		ConsPStack<Entry<K,V>> entries = getEntries(Objects.hashCode(key));
		while(entries != null && !entries.isEmpty()) {
			Entry<K,V> entry = entries.first;
			if (Objects.equals(entry.getKey(), key))
				return entry.getValue();
			entries = entries.rest;
		}
		return null;
	}

	
//// IMPLEMENTED METHODS OF PMap////
	public HashPMap<K,V> plusAll(final Map<? extends K, ? extends V> map) {
		HashPMap<K,V> result = this;
		for(Entry<? extends K,? extends V> entry : map.entrySet())
			result = result.plus(entry.getKey(), entry.getValue());
		return result;
	}

	public HashPMap<K,V> minusAll(final Collection<?> keys) {
		HashPMap<K,V> result = this;
		for(Object key : keys)
			result = result.minus(key);
		return result;
	}
	
	public HashPMap<K,V> plus(final K key, final V value) {
		ConsPStack<Entry<K,V>> entries = getEntries(Objects.hashCode(key));
		int size0 = entries.size(),
			i = keyIndexIn(entries, key);
		if(i!=-1) {
			if (Objects.equals(entries.get(i).getValue(), value))
				return this;
			entries = entries.minus(i);
		}
		entries = entries.plus(new org.pcollections.SimpleImmutableEntry<K,V>(key, value));
		return new HashPMap<K,V>(intMap.plus(Objects.hashCode(key), entries),
				size-size0+entries.size());
	}

	public HashPMap<K,V> minus(final Object key) {
		ConsPStack<Entry<K,V>> entries = getEntries(Objects.hashCode(key));
		int i = keyIndexIn(entries, key);
		if(i==-1) // key not in this
			return this;
		entries = entries.minus(i);
		if(entries.size()==0) // get rid of the entire hash entry
			return new HashPMap<K,V>(intMap.minus(Objects.hashCode(key)),
					size-1);
		// otherwise replace hash entry with new smaller one:
		return new HashPMap<K,V>(intMap.plus(Objects.hashCode(key), entries),
				size-1);
	}
	
	
//// PRIVATE UTILITIES ////
	private ConsPStack<Entry<K,V>> getEntries(final int hash) {
		ConsPStack<Entry<K,V>> entries = intMap.get(hash);
		if(entries==null) return ConsPStack.empty();
		return entries;
	}

	
//// PRIVATE STATIC UTILITIES ////
	private static <K,V> int keyIndexIn(final ConsPStack<Entry<K,V>> entries, final Object key) {
		int i=0;
		while(entries != null && !entries.isEmpty()) {
			Entry<K,V> entry = entries.first;
			if(Objects.equals(entry.getKey(), key))
				return i;
			i++;
		}
		return -1;
	}


	private static class SequenceIterator<E> implements Iterator<E> {
		private final Iterator<ConsPStack<E>> i;
		private PSequence<E> seq = ConsPStack.empty();
		SequenceIterator(Iterator<ConsPStack<E>> i) {
			this.i = i; }

		public boolean hasNext() {
			return seq.size()>0 || i.hasNext(); }

		public E next() {
			if(seq.size()==0)
				seq = i.next();
			final E result = seq.get(0);
			seq = seq.subList(1, seq.size());
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException(); }
	}
}
