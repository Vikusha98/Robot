package ru.netology;

import java.util.*;
import java.util.concurrent.*;

public class RobotRoute {
    public static final Map<Integer, Integer> sizeToFreq = new ConcurrentHashMap<>();

    private static class RouteGenerator implements Callable<Void> {
        private static final String letters = "RLRFR";
        private static final int length = 100;
        private static final Random random = new Random();

        @Override
        public Void call() {
            String route = generateRoute(letters, length);
            int freq = (int) route.chars().filter(ch -> ch == 'R').count();

            synchronized (sizeToFreq) {
                sizeToFreq.merge(freq, 1, Integer::sum);
            }

            return null;
        }

        private String generateRoute(String letters, int length) {
            StringBuilder route = new StringBuilder();
            for (int i = 0; i < length; i++) {
                route.append(letters.charAt(random.nextInt(letters.length())));
            }
            return route.toString();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = Collections.nCopies(1000, new RouteGenerator());
        executor.invokeAll(tasks);
        executor.shutdown();

        printFrequencies();
    }

    private static void printFrequencies() {
        Map.Entry<Integer, Integer> mostFrequent = sizeToFreq.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);

        if (mostFrequent != null) {
            System.out.printf("Самое частое количество повторений %d (встретилось %d раз)\n", mostFrequent.getKey(), mostFrequent.getValue());
        }

        System.out.println("Другие размеры:");
        sizeToFreq.entrySet().stream()
                .filter(entry -> !entry.equals(mostFrequent))
                .forEach(entry -> System.out.printf("- %d (%d раз)\n", entry.getKey(), entry.getValue()));
    }
}

