package it.unipi.dii.aide.mircv.query;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Priority queue implementation for maintaining the top-K elements.
 *
 * @param <E> Type of elements in the priority queue.
 */
public class TopKPriorityQueue<E> extends PriorityQueue<E> {
    private final int maxSize;

    /**
     * Constructor for TopKPriorityQueue.
     *
     * @param maxSize    The maximum size of the priority queue.
     * @param comparator The comparator used to order the elements.
     */
    public TopKPriorityQueue(int maxSize, Comparator<? super E> comparator) {
        super(maxSize, comparator);
        this.maxSize = maxSize;
    }

    /**
     * Adds the specified element to the priority queue if it is greater than the smallest element.
     *
     * @param e The element to be added.
     * @return True if the element is added, false otherwise.
     */
    @Override
    public boolean offer(E e) {
        // If the size exceeds the maximum size, compare with the smallest element.
        if (size() >= maxSize) {
            E top = peek();
            // If the new element is greater than the smallest element, replace the smallest element.
            if (comparator().compare(e, top) > 0) {
                poll(); // Remove the smallest element.
                super.offer(e); // Add the new element.
                return true; // Element added successfully.
            }
            return false; // Element not added.
        } else {
            // If the size is less than the maximum size, simply add the element.
            return super.offer(e);
        }
    }
}
