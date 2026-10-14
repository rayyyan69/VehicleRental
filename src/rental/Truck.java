package rental;

public class Truck extends Vehicle {
    private static final double LOADING_FEE = 500;
    private final double capacityTons;

    public Truck(String id, String brand, double ratePerDay, double capacityTons) {
        super(id, brand, ratePerDay);
        this.capacityTons = capacityTons;
    }

    @Override
    public String getType() { return "Truck"; }

    @Override
    public String getDetails() { return capacityTons + " tons"; }

    // Truck: rate x days + flat loading fee
    @Override
    public double calculateRent(int days) {
        return getRatePerDay() * days + LOADING_FEE;
    }
}
