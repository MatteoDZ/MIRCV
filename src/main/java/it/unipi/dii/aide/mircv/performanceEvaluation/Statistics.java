package it.unipi.dii.aide.mircv.performanceEvaluation;

import java.util.ArrayList;
import java.util.Collections;

public class Statistics {

    /**
     * Prints statistics of the given list including min, max, mean, and standard deviation.
     *
     * @param  label	description of the label for the statistics
     * @param  list	list of Long values to calculate statistics from
     */
    protected static void printStats(String label, ArrayList<Long> list) {
        if (list.isEmpty()) {
            System.out.println(label + " -> No data available");
            return;
        }
        long min = minimumOfTime(list);
        long max = maximumOfTime(list);
        double mean = averageOfTime(list);
        double stdDev = standardDeviationOfTime(list, mean);

        System.out.println(label + " -> Min: " + min + ", Max: " + max + ", Mean: " + mean + ", StdDev: " + stdDev);
    }

    /**
     * Returns the minimum value in the given list of long values.
     *
     * @param  list  the list of long values
     * @return       the minimum value in the list
     */
    private static long minimumOfTime(ArrayList<Long> list) {
        return Collections.min(list);
    }

    /**
     * Returns the maximum value in the given list of long values.
     *
     * @param  list  the list of long values
     * @return       the minimum value in the list
     */
    private static long maximumOfTime(ArrayList<Long> list) {
        return Collections.max(list);
    }

    /**
     * Calculate the average of a list of time values.
     *
     * @param  list  list of time values
     * @return       the average of the time values, or Double.NaN if the list is empty
     */
    private static double averageOfTime(ArrayList<Long> list) {
        return list.stream()
                .mapToDouble(Long::doubleValue)
                .average()
                .orElse(Double.NaN);
    }

    /**
     * Calculate the standard deviation of a list of time values given the mean.
     *
     * @param  list  the list of time values
     * @param  mean  the mean of the time values
     * @return       the standard deviation of the time values
     */
    private static double standardDeviationOfTime(ArrayList<Long> list, Double mean) {
        return Math.sqrt(list.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0));
    }

}
