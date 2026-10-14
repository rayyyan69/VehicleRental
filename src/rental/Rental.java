package rental;

import java.time.LocalDate;

public class Rental {
    private final int rentalId;
    private final Customer customer;
    private final Vehicle vehicle;
    private final int days;
    private final double totalRent;
    private final LocalDate startDate = LocalDate.now();
    private boolean returned = false;

    public Rental(int rentalId, Customer customer, Vehicle vehicle, int days) {
        this.rentalId = rentalId;
        this.customer = customer;
        this.vehicle = vehicle;
        this.days = days;
        this.totalRent = vehicle.calculateRent(days);
    }

    public int getRentalId() { return rentalId; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public int getDays() { return days; }
    public double getTotalRent() { return totalRent; }
    public LocalDate getStartDate() { return startDate; }
    public boolean isReturned() { return returned; }
    public void markReturned() { this.returned = true; }
}
