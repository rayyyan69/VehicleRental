package rental;

public abstract class Vehicle implements Rentable {
    private final String vehicleId;
    private final String brand;
    private final double ratePerDay;
    private boolean isAvailable = true;

    protected Vehicle(String vehicleId, String brand, double ratePerDay) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.ratePerDay = ratePerDay;
    }

    public String getVehicleId() { return vehicleId; }
    public String getBrand() { return brand; }
    public double getRatePerDay() { return ratePerDay; }

    @Override
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    public abstract String getType();
    public abstract String getDetails();

    @Override
    public abstract double calculateRent(int days);
}
