import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class AntColonyAlgorithm {
    private final Double Q_CYCL = 100.0;
    private final Double EVAPORATION = 0.5;
    private final Double Q_DAS = 0.55;
    private final Double ALPHA = 1.0;
    private final Double BETA = 3.0;
    private final Double RANDOM_PR_VALUE = 0.3;
    private List<Double> probability = new ArrayList<>();
    private int size;
    private int M; // number of ants
    private List<Integer> bestPath;
    private Integer bestCost;
    private Double pheroValue;
    private List<Ant> ants = new ArrayList<>();
    List<List<Double>> pheromoneMatrix = new ArrayList<>();
    List<List<Integer>> cities = new ArrayList<>();
    Random random = new Random();

    static Semaphore semaphore = new Semaphore(1);

    public AntColonyAlgorithm() {

    }

    public int startColonyAlgorithm(int iterations) {
        for (int i = 0; i < iterations; i++) {
            M = size;
            initAnts(size);
            initProbabilityList(size);
            pheroValue = M / calculateApproximatePathCost();
            initPheroMatrix();
            try {
                runAllAnts();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return bestCost;
    }

    private void runAllAnts() throws InterruptedException {
//        Thread t1 = new Thread(() -> {
//            for (int i = 0; i < size / 2; i++) {
//                runOneAnt(i);
//            }
//        });
//        Thread t2 = new Thread(() -> {
//            for (int i = size / 2; i < size; i++) {
//                runOneAnt(i);
//            }
//        });
//
//        t1.start();
//        t2.start();

        for (int i = 0; i < size; i++) {
            runOneAnt(i);
        }
//
//        t1.join();
//        t2.join();
//

        placePheromoneCAS();
        ants.clear();
    }

    private void placePheromoneCAS() {
        int cost = 0;
        int min = Integer.MAX_VALUE;
        List<Integer> greedyBestPath = new ArrayList<>();

        for (Ant ant: ants) {
            cost = ant.getTotalDistance();

            if (cost < min) {
                min = cost;
                greedyBestPath = ant.getPath();
            }

            for (int i = 1; i < greedyBestPath.size(); i++) {
                int from = greedyBestPath.get(i - 1);
                int to = greedyBestPath.get(i);
                double currentValue = pheromoneMatrix.get(from).get(to) ;
                pheromoneMatrix.get(from).set(to, currentValue * (Q_CYCL / cost));
            }
        }
    }

    private void runOneAnt(int antIndex) {
        Ant ant = ants.get(antIndex);
        reducePheromone();
        int currentCity, nextCity, initialCity;

        while (!ant.checkIfAllCitiesAreVisited()) {
            currentCity = ant.getCurrentCity();
            nextCity = chooseCity(antIndex);
            ant.move(nextCity);
            ant.addDistance(cities.get(currentCity).get(nextCity));
        }
        currentCity = ant.getCurrentCity();
        initialCity = ant.getInitialCity();
        ant.move(initialCity);
        ant.addDistance(cities.get(currentCity).get(initialCity));

        if (bestCost > ant.getTotalDistance()) {
            bestCost = ant.getTotalDistance();
            bestPath = ant.getPath();
        }
    }

    private int chooseCity(int antIndex) {
        Ant ant = ants.get(antIndex);
        int currentCity = ant.getCurrentCity();

        if (getRandomDouble(0.01, 0.5) < RANDOM_PR_VALUE) {
            int maxU = -1;
            double maxV = -1;

            for (int city = 0; city < size; city++) {
                if (ant.checkIfCityIsVisited(city)) {
                    continue;
                }
                double probabilityOfChoosingCity =
                        Math.pow(pheromoneMatrix.get(currentCity).get(city), ALPHA) *
                                Math.pow(1.0 / cities.get(currentCity).get(city), BETA);
                if (probabilityOfChoosingCity > maxV) {
                    maxV = probabilityOfChoosingCity;
                    maxU = city;
                }
            }
            return maxU;
        }
        calculateProbabilityOfChoosingCity(ant);
        double randomDouble = getRandomDouble(0.45, 1.0);

        while(true) {
            int city = random.nextInt(Integer.MAX_VALUE) % size;
            double probabilityToVisitCity = probability.get(city);

            if (probabilityToVisitCity >= randomDouble && !ant.checkIfCityIsVisited(city)) {
                return city;
            }

            if (probabilityToVisitCity != 0.0) {
                probability.set(city, probability.get(city) + 0.1);
            }
        }
    }

    private void calculateProbabilityOfChoosingCity(Ant ant) {
        int currentCity = ant.getCurrentCity();
        double nominator = 0.0;
        double denominator = 0.0;

        for (int city = 0; city < size; city++) {
            if (ant.getCurrentCity() == city) continue;
            if (ant.checkIfCityIsVisited(city)) continue;

            double tau = Math.pow(pheromoneMatrix.get(currentCity).get(city), ALPHA);
            double eta;
            if (cities.get(currentCity).get(city) != 0) {
                eta = Math.pow(1.0 / cities.get(currentCity).get(city), BETA);
            } else {
                eta = Math.pow(1.0 / 0.1, BETA);
            }
            denominator += tau * eta;
        }

        for (int city = 0; city < size; city++) {
            if (ant.checkIfCityIsVisited(city)) {
                probability.set(city, 0.0);
            } else if (cities.get(currentCity).get(city) != 0) {
                nominator = Math.pow(pheromoneMatrix.get(currentCity).get(city), ALPHA) *
                        Math.pow(1.0 / cities.get(currentCity).get(city), BETA);
                probability.set(city, nominator / denominator);
            } else if (cities.get(currentCity).get(city) == 0) {
                nominator = Math.pow(pheromoneMatrix.get(currentCity).get(city), ALPHA) *
                        Math.pow(1.0 / 0.001, BETA);
                probability.set(city, nominator / denominator);
            }
        }
    }

    public Double getRandomDouble(double rangeFrom, double rangeTo) {
        return random.nextDouble(rangeFrom, rangeTo);
    }

    private void reducePheromone() {
        double value = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                value = pheromoneMatrix.get(i).get(j);
                pheromoneMatrix.get(i).set(j, value * (1 - EVAPORATION));
            }
        }
    }

    private void initPheroMatrix() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                pheromoneMatrix.get(i).set(j, pheroValue);
            }
        }
    }

    private void initAnts(int size) {
        for (int i = 0; i < size; i++) {
            ants.add(new Ant(size, i));
        }
    }

    private void initProbabilityList(int size) {
        probability.clear();
        for (int i = 0; i < size; i++) {
            probability.add(0.0001);
        }
    }

    private double calculateApproximatePathCost() {
        int iterations = 100;
        int totalCost = 0;

        for (int i = 0; i < iterations; i++) {
            List<Integer> path = randomPathPermutation();
            totalCost += calculatePath(path);
        }
        return (double) totalCost / iterations;
    }


    private Integer calculatePath(List<Integer> path) {
        int cost = 0;
        for(int i = 0; i < path.size() -1; i++) {
            cost += cities.get(path.get(i)).get(path.get(i + 1));
        }
        cost += cities.get(path.get(size - 1)).get(path.get(0));
        return cost;
    }

    private List<Integer> randomPathPermutation() {
        List<Integer> randomPath = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            randomPath.add(i);
        }
        Collections.shuffle(randomPath);
        return randomPath;
    }


    public List<List<Double>> getPheromoneMatrix() {
        return pheromoneMatrix;
    }

    public void setPheromoneMatrix(List<List<Double>> pheromoneMatrix) {
        this.pheromoneMatrix = pheromoneMatrix;
    }

    public List<List<Integer>> getCities() {
        return cities;
    }

    public void setCities(List<List<Integer>> cities) {
        this.cities = cities;
    }

    public List<Integer> getBestPath() {
        return bestPath;
    }

    public void setBestPath(List<Integer> bestPath) {
        this.bestPath = bestPath;
    }

    public Integer getBestCost() {
        return bestCost;
    }

    public void setBestCost(Integer bestCost) {
        this.bestCost = bestCost;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
