import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CitiesReader {

    private static final String INSTANCE = "instancje/";
    private static List<List<Integer>> cities;
    private static List<List<Double>> pheromoneMatrix;

//    private static AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm();

    private static Double time;
    private static Double error;

    private static Integer solution;

    public static void main(String[] args) {
        loadINIFile();
    }

    private static void runAlgorithm(String fileName, int iterations, int optimalCost, List<Integer> optimalPath) {
        for (int i = 0; i < iterations; i++) {
            loadFromFile(fileName);

            System.out.println("Plik: " + fileName);
            System.out.println("Optymalny wynik: " + optimalCost);
            System.out.println("Optymalna ścieżka: " + optimalPath);
            AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm();
            antColonyAlgorithm.setSize(cities.size());
            antColonyAlgorithm.setBestCost(Integer.MAX_VALUE);
            antColonyAlgorithm.setBestPath(new ArrayList<>());
            antColonyAlgorithm.setCities(cities);
            antColonyAlgorithm.setPheromoneMatrix(pheromoneMatrix);
            long start = System.currentTimeMillis();
            solution = antColonyAlgorithm.startColonyAlgorithm(iterations);
            time = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println("Czas wykonania: " + time);
            error = calculateRelativeError(solution, optimalCost);
            System.out.println("Znaleziona droga: " + antColonyAlgorithm.getBestPath());
            System.out.println("Wynik: " + solution);
            System.out.println("Błąd: " + error);
        }
        saveToCsv(fileName, iterations, optimalCost, optimalPath, solution, time, error);
    }

    private static void loadINIFile() {
        String filename = "Badania.INI";
        String fileToRead;
        int iterations;
        int optimalCost;
        List<Integer> optimalPath;
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> parsedLine = List.of(line.split(" "));

                fileToRead = INSTANCE + parsedLine.get(0);
                iterations = Integer.parseInt(parsedLine.get(1));
                optimalCost = Integer.parseInt(parsedLine.get(2));
                optimalPath = new ArrayList<>();
                List<String> path = parsedLine.subList(3, parsedLine.size());

                for (String city: path) {
                    if(city.contains("?")) {
                        break;
                    }

                    if (city.contains("[") || city.contains("]")) {
                        city = city.replace("[", "");
                        city = city.replace("]", "");
                    }
                    optimalPath.add(Integer.parseInt(city));
                }
//                runAlgorithm(fileToRead, iterations, optimalCost, optimalPath);
                String finalFileToRead = fileToRead;
                int finalIterations = iterations;
                int finalOptimalCost = optimalCost;
                List<Integer> finalOptimalPath = optimalPath;
                Thread t1 = new Thread(() -> {
                    runAlgorithm(finalFileToRead, finalIterations / 4, finalOptimalCost, finalOptimalPath);
                });

                Thread t2 = new Thread(() -> {
                    runAlgorithm(finalFileToRead, finalIterations / 4, finalOptimalCost, finalOptimalPath);
                });
                Thread t3 = new Thread(() -> {
                    runAlgorithm(finalFileToRead, finalIterations / 4, finalOptimalCost, finalOptimalPath);
                });

                Thread t4 = new Thread(() -> {
                    runAlgorithm(finalFileToRead, finalIterations / 4, finalOptimalCost, finalOptimalPath);
                });

                t1.start();
                t2.start();
                t3.start();
                t4.start();

//                t1.join();
//                t2.join();
//                t3.join();
//                t4.join();
            }
            scanner.close();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static void loadFromFile(String fileName) {
        List<List<Integer>> initCities = new ArrayList<>();
        List<List<Double>> initPheromoneMatrix = new ArrayList<>();
        int size = 0;
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            if (scanner.hasNextLine()) {
               String numberOfCities = scanner.nextLine();
               size = Integer.parseInt(numberOfCities);
               System.out.println(size);
            } else {
                throw new RuntimeException();
            }

            for (int i = 0; i < size; i++) {
                String readLine = scanner.nextLine();
                List<String> row = List.of(readLine.split(" "));
                List<Integer> rowInt = row.stream()
                        .filter(number -> !number.isBlank())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                initCities.add(rowInt);
            }

            for (int i = 0; i < size; i++) {
                initPheromoneMatrix.add(new ArrayList<>());
                for (int j = 0; j < size; j++) {
                    initPheromoneMatrix.get(i).add(0.0);
                    if (i == j) {
                        initCities.get(i).set(j, Integer.MAX_VALUE);
                    }
                }
            }

            cities = initCities;
            pheromoneMatrix = initPheromoneMatrix;
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveToCsv(
        String fileName,
        int iterations,
        int optimalSolution,
        List<Integer> optimalPath,
        Integer solution,
        Double time,
        Double error) {

        String csvFileName =  "result.csv";

        try {
            FileWriter fileWriter = new FileWriter(csvFileName, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.print(fileName + ";");
            printWriter.print(optimalSolution + ";");
            printWriter.print(iterations + ";");
            printWriter.println(optimalPath + ";");
            printWriter.print(time + ";");
            printWriter.print(solution + ";");
            printWriter.println(error + ";");

            printWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static double calculateRelativeError(int result, int optimalSolution) {
        return ((Math.abs((double)optimalSolution - (double)result) / (double)optimalSolution) * 100);
    }
}
