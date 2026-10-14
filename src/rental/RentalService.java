package rental;

import java.util.ArrayList;
import java.util.List;

public class RentalService {
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();
    private int nextRentalId = 1;

    public synchronized List<Vehicle> getVehicles() { return new ArrayList<>(vehicles); }
    public synchronized List<Customer> getCustomers() { return new ArrayList<>(customers); }
    public synchronized List<Rental> getRentals() { return new ArrayList<>(rentals); }

    public synchronized void addVehicle(Vehicle v) {
        if (findVehicle(v.getVehicleId()) != null)
            throw new IllegalArgumentException("Vehicle ID already exists: " + v.getVehicleId());
        vehicles.add(v);
    }

    public synchronized void addCustomer(Customer c) {
        if (findCustomer(c.getCustomerId()) != null)
            throw new IllegalArgumentException("Customer ID already exists: " + c.getCustomerId());
        customers.add(c);
    }

    public synchronized double quote(String vehicleId, int days) {
        Vehicle v = findVehicle(vehicleId);
        if (v == null) throw new IllegalArgumentException("No such vehicle: " + vehicleId);
        if (days <= 0) throw new IllegalArgumentException("Days must be at least 1");
        return v.calculateRent(days);
    }

    public synchronized Rental rent(String vehicleId, String customerId, int days) {
        Vehicle v = findVehicle(vehicleId);
        Customer c = findCustomer(customerId);
        if (v == null) throw new IllegalArgumentException("No such vehicle: " + vehicleId);
        if (c == null) throw new IllegalArgumentException("No such customer: " + customerId);
        if (days <= 0) throw new IllegalArgumentException("Days must be at least 1");
        if (!v.isAvailable()) throw new IllegalArgumentException("Vehicle is already rented");
        v.setAvailable(false);
        Rental r = new Rental(nextRentalId++, c, v, days);
        rentals.add(r);
        return r;
    }

    public synchronized Rental returnVehicle(int rentalId) {
        for (Rental r : rentals) {
            if (r.getRentalId() == rentalId) {
                if (r.isReturned()) throw new IllegalArgumentException("Already returned");
                r.markReturned();
                r.getVehicle().setAvailable(true);
                return r;
            }
        }
        throw new IllegalArgumentException("No such rental: " + rentalId);
    }

    public Vehicle findVehicle(String id) {
        for (Vehicle v : vehicles) if (v.getVehicleId().equalsIgnoreCase(id)) return v;
        return null;
    }

    public Customer findCustomer(String id) {
        for (Customer c : customers) if (c.getCustomerId().equalsIgnoreCase(id)) return c;
        return null;
    }
}
