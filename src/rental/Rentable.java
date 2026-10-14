package rental;

public interface Rentable {
    double calculateRent(int days);

    boolean isAvailable();
}
