package rental;

public class Bike extends Vehicle {
    private final int engineCc;

    public Bike(String id, String brand, double ratePerDay, int engineCc) {
        super(id, brand, ratePerDay);
        this.engineCc = engineCc;
    }

    @Override
    public String getType() { return "Bike"; }

    @Override
    public String getDetails() { return engineCc + " cc"; }

    // Bike: 10% discount for rentals of 7 days or more
    @Override
    public double calculateRent(int days) {
        double total = getRatePerDay() * days;
        return days >= 7 ? total * 0.90 : total;
    }
}
