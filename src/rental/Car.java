package rental;

public class Car extends Vehicle {
    private final int seats;

    public Car(String id, String brand, double ratePerDay, int seats) {
        super(id, brand, ratePerDay);
        this.seats = seats;
    }

    @Override
    public String getType() { return "Car"; }

    @Override
    public String getDetails() { return seats + " seats"; }

    // Car: plain rate x days
    @Override
    public double calculateRent(int days) {
        return getRatePerDay() * days;
    }
}
