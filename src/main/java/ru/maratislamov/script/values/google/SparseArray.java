package ru.maratislamov.script.values.google;
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import ru.maratislamov.script.values.google.util.ContainerHelpers;
import ru.maratislamov.script.values.google.util.GrowingArrayUtils;
import ru.maratislamov.script.values.google.util.ArrayUtils;

import java.rmi.UnexpectedException;
import java.util.*;

/**
 * <code>SparseArray</code> maps integers to Objects and, unlike a normal array of Objects,
 * its indices can contain gaps. <code>SparseArray</code> is intended to be more memory-efficient
 * than a
 * <a href="/reference/java/util/HashMap"><code>HashMap</code></a>, because it avoids
 * auto-boxing keys and its data structure doesn't rely on an extra entry object
 * for each mapping.
 *
 * <p>Note that this container keeps its mappings in an array data structure,
 * using a binary search to find keys. The implementation is not intended to be appropriate for
 * data structures
 * that may contain large numbers of items. It is generally slower than a
 * <code>HashMap</code> because lookups require a binary search,
 * and adds and removes require inserting
 * and deleting entries in the array. For containers holding up to hundreds of items,
 * the performance difference is less than 50%.
 *
 * <p>To help with performance, the container includes an optimization when removing
 * keys: instead of compacting its array immediately, it leaves the removed entry marked
 * as deleted. The entry can then be re-used for the same key or compacted later in
 * a single garbage collection of all removed entries. This garbage collection
 * must be performed whenever the array needs to be grown, or when the map size or
 * entry values are retrieved.
 *
 * <p>It is possible to iterate over the items in this container using
 * { @ link #keyAt(int)} and { @ link #valueAt(int)}. Iterating over the keys using
 * <code>keyAt(int)</code> with ascending values of the index returns the
 * keys in ascending order. In the case of <code>valueAt(int)</code>, the
 * values corresponding to the keys are returned in ascending order.
 */
public class SparseArray<E> extends AbstractList<E> implements Cloneable {
    private static final Object DELETED = new Object();
    private boolean mGarbage = false;

    private int[] mKeys;

    private Object[] mValues;

    private int mSize;

    /**
     * Creates a new SparseArray containing no mappings.
     */
    public SparseArray() {
        this(10);
    }

    /**
     * Creates a new SparseArray containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.  If you supply an initial capacity of 0, the
     * sparse array will be initialized with a light-weight representation
     * not requiring any additional array allocations.
     */
    public SparseArray(int initialCapacity) {
        if (initialCapacity == 0) {
            mKeys = new int[]{};
            mValues = new Object[]{};
        } else {
            mValues = ArrayUtils.newUnpaddedObjectArray(initialCapacity);
            mKeys = new int[mValues.length];
        }
        Arrays.fill(mKeys, -1);
        mSize = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SparseArray<E> clone() {
        SparseArray<E> clone = null;
        try {
            clone = (SparseArray<E>) super.clone();
            clone.mKeys = mKeys.clone();
            clone.mValues = mValues.clone();
        } catch (CloneNotSupportedException cnse) {
            /* ignore */
        }
        return clone;
    }

    /**
     * Returns true if the key exists in the array. This is equivalent to
     * { @ link #indexOfKey(int)} >= 0.
     *
     * @param key Potential key in the mapping
     * @return true if the key is defined in the mapping
     */
    public boolean contains(int key) {
        return indexOfKey(key) >= 0;
    }

    /**
     * Gets the Object mapped from the specified key, or <code>null</code>
     * if no such mapping has been made.
     */
    public E get(int key) {
        return get(key, null);
    }

    /**
     * Gets the Object mapped from the specified key, or the specified Object
     * if no such mapping has been made.
     */
    @SuppressWarnings("unchecked")
    public E get(int key, E valueIfKeyNotFound) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i < 0 || mValues[i] == DELETED) {
            return valueIfKeyNotFound;
        } else {
            return (E) mValues[i];
        }
    }

    /**
     * Removes the mapping from the specified key, if there was any.
     */
    public void delete(int key) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i >= 0) {
            if (mValues[i] != DELETED) {
                mValues[i] = DELETED;
                mGarbage = true;
            }
        }
    }

    /**
     * @hide Removes the mapping from the specified key, if there was any, returning the old value.
     */
    public E removeReturnOld(int key) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i >= 0) {
            if (mValues[i] != DELETED) {
                final E old = (E) mValues[i];
                mValues[i] = DELETED;
                mGarbage = true;
                return old;
            }
        }
        return null;
    }

    /**
     * Alias for { @ link #delete(int)}.
     */
    public E remove(int key) {
        return removeReturnOld(key);
    }

    /**
     * Removes the mapping at the specified index.
     *
     * <p>For indices outside of the range <code>0...size()-1</code>,
     * the behavior is undefined for apps targeting {link android.os.Build.VERSION_CODES#P} and
     * earlier, and an { @ link ArrayIndexOutOfBoundsException} is thrown for apps targeting
     * {link android.os.Build.VERSION_CODES#Q} and later.</p>
     */
    public void removeAt(int index) {
        if (index >= mSize && UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            // Check if exception should be thrown outside of the critical path.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (mValues[index] != DELETED) {
            mValues[index] = DELETED;
            mGarbage = true;
        }
    }

    /**
     * Remove a range of mappings as a batch.
     *
     * @param index Index to begin at
     * @param size  Number of mappings to remove
     *
     *              <p>For indices outside of the range <code>0...size()-1</code>,
     *              the behavior is undefined.</p>
     */
    public void removeAtRange(int index, int size) {
        final int end = Math.min(mSize, index + size);
        for (int i = index; i < end; i++) {
            removeAt(i);
        }
    }

    private void gc() {
        // Log.e("SparseArray", "gc start with " + mSize);
        int n = mSize;
        int o = 0;
        int[] keys = mKeys;
        Object[] values = mValues;
        for (int i = 0; i < n; i++) {
            Object val = values[i];
            if (val != DELETED) {
                if (i != o) {
                    keys[o] = keys[i];
                    values[o] = val;
                    values[i] = null;
                }
                o++;
            }
        }
        mGarbage = false;
        mSize = o;
        // Log.e("SparseArray", "gc end with " + mSize);
    }

    /**
     * Alias for { @ link #put(int, Object)} to support Kotlin [index]= operator.
     *
     * @see #put(int, Object)
     */
    public E set(int key, E value) {
        final E old = get(key);
        put(key, value);
        return old;
    }

    /**
     * Adds a mapping from the specified key to the specified value,
     * replacing the previous mapping from the specified key if there
     * was one.
     */
    public void put(int key, E value) {
        if (key < 0) throw new RuntimeException("index < 0: " + key);

        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i >= 0) {
            mValues[i] = value;
        } else {
            i = ~i;
            if (i < mSize && mValues[i] == DELETED) {
                mKeys[i] = key;
                mValues[i] = value;
                return;
            }
            if (mGarbage && mSize >= mKeys.length) {
                gc();
                // Search again because indices may have changed.
                i = ~ContainerHelpers.binarySearch(mKeys, mSize, key);
            }
            mKeys = GrowingArrayUtils.insert(mKeys, mSize, i, key);
            mValues = GrowingArrayUtils.insert(mValues, mSize, i, value);
            mSize++;
        }
    }

    public void push(E value) {
        put(mSize, value);
    }

    /**
     * Returns the number of key-value mappings that this SparseArray
     * currently stores.
     */
    public int size() {
        if (mGarbage) {
            gc();
        }
        //return mSize;
        return maxIndex() + 1;
    }

    /**
     * max index = size of implemented by sparse array.
     *
     * @return
     */
    public int maxIndex() {
        // todo: оптимизировать! хранить максимальный отдельно, отслеживать при удалении, обновлять при сборке мусора
        int best = -1;
        for (int i = 0; i < mKeys.length; i++) {
            int mKey = mKeys[i];
            if (mValues[i] != DELETED && mKey > best) {
                best = mKey;
            }
        }
        return best;
    }

    public int arraySize() {

        return maxIndex() + 1;
    }

    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the key from the <code>index</code>th key-value mapping that this
     * SparseArray stores.
     *
     * <p>The keys corresponding to indices in ascending order are guaranteed to
     * be in ascending order, e.g., <code>keyAt(0)</code> will return the
     * smallest key and <code>keyAt(size()-1)</code> will return the largest
     * key.</p>
     *
     * <p>For indices outside of the range <code>0...size()-1</code>,
     * the behavior is undefined for apps targeting { @ link android.os.Build.VERSION_CODES#P} and
     * earlier, and an { @ link ArrayIndexOutOfBoundsException} is thrown for apps targeting
     * { @ link android.os.Build.VERSION_CODES#Q} and later.</p>
     */
    public int keyAt(int index) {
        if (index >= mSize && UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            // Check if exception should be thrown outside of the critical path.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (mGarbage) {
            gc();
        }
        return mKeys[index];
    }

    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the value from the <code>index</code>th key-value mapping that this
     * SparseArray stores.
     *
     * <p>The values corresponding to indices in ascending order are guaranteed
     * to be associated with keys in ascending order, e.g.,
     * <code>valueAt(0)</code> will return the value associated with the
     * smallest key and <code>valueAt(size()-1)</code> will return the value
     * associated with the largest key.</p>
     *
     * <p>For indices outside of the range <code>0...size()-1</code>,
     * the behavior is undefined for apps targeting { @ link android.os.Build.VERSION_CODES#P} and
     * earlier, and an { @ link ArrayIndexOutOfBoundsException} is thrown for apps targeting
     * { @ link android.os.Build.VERSION_CODES#Q} and later.</p>
     */
    @SuppressWarnings("unchecked")
    public E valueAt(int index) {
        if (index >= mSize && UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            // Check if exception should be thrown outside of the critical path.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (mGarbage) {
            gc();
        }
        return (E) mValues[index];
    }

    /**
     * Given an index in the range <code>0...size()-1</code>, sets a new
     * value for the <code>index</code>th key-value mapping that this
     * SparseArray stores.
     *
     * <p>For indices outside of the range <code>0...size()-1</code>, the behavior is undefined for
     * apps targeting { @ link android.os.Build.VERSION_CODES#P} and earlier, and an
     * { @ link ArrayIndexOutOfBoundsException} is thrown for apps targeting
     * { @ link android.os.Build.VERSION_CODES#Q} and later.</p>
     */
    public void setValueAt(int index, E value) {
        if (index >= mSize && UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            // Check if exception should be thrown outside of the critical path.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (mGarbage) {
            gc();
        }
        mValues[index] = value;
    }

    /**
     * Returns the index for which { @ link #keyAt} would return the
     * specified key, or a negative number if the specified
     * key is not mapped.
     */
    public int indexOfKey(int key) {
        if (mGarbage) {
            gc();
        }
        return ContainerHelpers.binarySearch(mKeys, mSize, key);
    }

    /**
     * Returns an index for which { @ link #valueAt} would return the
     * specified value, or a negative number if no keys map to the
     * specified value.
     * <p>Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     * <p>Note also that unlike most collections' {@code indexOf} methods,
     * this method compares values using {@code ==} rather than {@code equals}.
     */
    public int indexOfValue(E value) {
        if (mGarbage) {
            gc();
        }
        for (int i = 0; i < mSize; i++) {
            if (mValues[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns an index for which { @ link #valueAt} would return the
     * specified value, or a negative number if no keys map to the
     * specified value.
     * <p>Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     * <p>Note also that this method uses {@code equals} unlike {@code indexOfValue}.
     *
     * @hide
     */
    public int indexOfValueByValue(E value) {
        if (mGarbage) {
            gc();
        }
        for (int i = 0; i < mSize; i++) {
            if (value == null) {
                if (mValues[i] == null) {
                    return i;
                }
            } else {
                if (value.equals(mValues[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Removes all key-value mappings from this SparseArray.
     */
    public void clear() {
        int n = mSize;
        Object[] values = mValues;
        for (int i = 0; i < n; i++) {
            values[i] = null;
        }
        mSize = 0;
        mGarbage = false;
    }

    /**
     * Puts a key/value pair into the array, optimizing for the case where
     * the key is greater than all existing keys in the array.
     */
    public void append(int key, E value) {
        if (mSize != 0 && key <= mKeys[mSize - 1]) {
            put(key, value);
            return;
        }
        if (mGarbage && mSize >= mKeys.length) {
            gc();
        }
        mKeys = GrowingArrayUtils.append(mKeys, mSize, key);
        mValues = GrowingArrayUtils.append(mValues, mSize, value);
        mSize++;
    }

    public void add(int index, E element) {
        append(index, element);
    }

    public boolean add(E e) {
        add(maxIndex() + 1, e);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating over its mappings. If
     * this map contains itself as a value, the string "(this Map)"
     * will appear in its place.
     */
    @Override
    public String toString() {
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(mSize * 28);
        buffer.append('[');
        for (int i = 0; i < mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keyAt(i);
            buffer.append(key);
            buffer.append("=");
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Compares the contents of this { @ link SparseArray} to the specified { @ link SparseArray}.
     * <p>
     * For backwards compatibility reasons, { @ link Object#equals(Object)} cannot be implemented,
     * so this serves as a manually invoked alternative.
     */
    public boolean contentEquals(SparseArray<?> other) {
        if (other == null) {
            return false;
        }
        int size = size();
        if (size != other.size()) {
            return false;
        }
        for (int index = 0; index < size; index++) {
            int key = keyAt(index);
            if (!Objects.equals(valueAt(index), other.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code value for the contents of this { @ link SparseArray}, combining the
     * { @ link Objects#hashCode(Object)} result of all its keys and values.
     * <p>
     * For backwards compatibility, { @ link Object#hashCode()} cannot be implemented, so this serves
     * as a manually invoked alternative.
     */
    public int contentHashCode() {
        int hash = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            int key = keyAt(index);
            E value = valueAt(index);
            hash = 31 * hash + Objects.hashCode(key);
            hash = 31 * hash + Objects.hashCode(value);
        }
        return hash;
    }


}
