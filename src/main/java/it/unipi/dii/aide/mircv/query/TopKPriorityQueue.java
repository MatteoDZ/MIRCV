package it.unipi.dii.aide.mircv.query;

import org.javatuples.Pair;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Priority queue implementation for keeping the topK documents obtained in a query.
 *
 * @param <T> Type of elements in the priority queue.
 */
public class TopKPriorityQueue<T> extends PriorityQueue<T> {

    private final int size;

    /**
     * Constructor for TopKPriorityQueue.
     *
     * @param size    The maximum size of the priority queue.
     * @param comparator The comparator used to order the elements.
     */
    public TopKPriorityQueue(int size, Comparator<? super T> comparator) {
        super(size, comparator);
        this.size = size;
    }

    /**
     * Adds the element e to the TopKPriorityQueue if it is better than the worst element.
     *
     * @param e The element to be added.
     * @return True if the element is added, false otherwise.
     */
    @Override
    public boolean offer(T e) {
        if (this.size() >= size) {
            T lowest = this.peek();
            if (comparator().compare(e, lowest) <= 0) {
                return false;
            }
            this.poll();
        }
        return super.offer(e);
    }
}
