import java.util.ArrayList;
import java.util.List;

public class Ant {
    private List<Integer> path = new ArrayList<>();
    private List<Boolean> visitedCities;
    private int initialCity;
    private int totalDistance;
    private int currentCity;
    private int size;

    public Ant(int size, int initialCity) {
        this.size = size;
        this.initialCity = initialCity;
        this.currentCity = initialCity;
        totalDistance = 0;
        this.visitedCities = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            this.visitedCities.add(false);
        }
        this.visitedCities.set(initialCity, true);
        this.path.add(initialCity);
    }

    public void move(int destination) {
        currentCity = destination;
        path.add(currentCity);
        visitedCities.set(destination, true);
    }

    public void addDistance(int distance) {
        setTotalDistance(getTotalDistance() + distance);
    }

    public boolean checkIfCityIsVisited(int city) {
        return visitedCities.get(city);
    }

    public boolean checkIfAllCitiesAreVisited() {
        return path.size() == size;
    }

    public void clearData() {
        visitedCities = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            visitedCities.add(false);
        }
        path.clear();
    }


    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    public List<Boolean> getVisitedCities() {
        return visitedCities;
    }

    public void setVisitedCities(List<Boolean> visitedCities) {
        this.visitedCities = visitedCities;
    }

    public int getInitialCity() {
        return initialCity;
    }

    public void setInitialCity(int initialCity) {
        this.initialCity = initialCity;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(int currentCity) {
        this.currentCity = currentCity;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
