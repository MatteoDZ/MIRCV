package it.unipi.dii.aide.mircv.query;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Priority queue implementation for keeping the topK results obtained in a queru.
 *
 * @param <T> Type of elements in the priority queue.
 */
public class TopKPriorityQueue<T> extends PriorityQueue<T> {
    private final int maxSize;

    /**
     * Constructor for TopKPriorityQueue.
     *
     * @param maxSize    The maximum size of the priority queue.
     * @param comparator The comparator used to order the elements.
     */
    public TopKPriorityQueue(int maxSize, Comparator<? super T> comparator) {
        super(maxSize, comparator);
        this.maxSize = maxSize;
    }

    /**
     * Adds the element e to the TopKPriorityQueue if it is better than the worst element.
     *
     * @param e The element to be added.
     * @return True if the element is added, false otherwise.
     */
    @Override
    public boolean offer(T e) {
        // If the priority queue size is smaller than maxSize directly add the element
        if (size() < maxSize) {
            return super.offer(e);
        }
        // Else check if the element e is better than the worst element present in the priority queue
        else {
            T top = peek();
            // If the new element is better than the worst element, replace the worst element with the new one.
            if (comparator().compare(e, top) > 0) {
                poll();
                return super.offer(e); // Element added
            }
            return false; // Element not added.
        }
    }
}
