package rental;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    private static final int PORT = 8090;
    private static final RentalService service = new RentalService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", Main::serveStatic);
        server.createContext("/api/vehicles", ex -> api(ex, Main::vehicles));
        server.createContext("/api/customers", ex -> api(ex, Main::customers));
        server.createContext("/api/rentals", ex -> api(ex, Main::rentals));
        server.createContext("/api/rent", ex -> api(ex, Main::rent));
        server.createContext("/api/return", ex -> api(ex, Main::returnVehicle));
        server.createContext("/api/quote", ex -> api(ex, Main::quote));

        server.start();
        System.out.println("Vehicle Rental System running.");
        System.out.println("Open http://localhost:" + PORT + " in your browser.");
    }

    // ---------- API handlers: each returns a JSON string ----------

    private static String vehicles(String method, Map<String, String> p) {
        if (method.equals("POST")) {
            String type = p.getOrDefault("type", "");
            String id = required(p, "id");
            String brand = required(p, "brand");
            double rate = Double.parseDouble(required(p, "rate"));
            String extra = required(p, "extra");
            if (rate <= 0) throw new IllegalArgumentException("Rate must be positive");
            Vehicle v;
            switch (type) {
                case "Car" -> v = new Car(id, brand, rate, Integer.parseInt(extra));
                case "Bike" -> v = new Bike(id, brand, rate, Integer.parseInt(extra));
                case "Truck" -> v = new Truck(id, brand, rate, Double.parseDouble(extra));
                default -> throw new IllegalArgumentException("Choose a vehicle type");
            }
            service.addVehicle(v);
        }
        return json(service.getVehicles(), v ->
                "{\"id\":" + q(v.getVehicleId()) + ",\"type\":" + q(v.getType())
                        + ",\"brand\":" + q(v.getBrand()) + ",\"details\":" + q(v.getDetails())
                        + ",\"rate\":" + v.getRatePerDay() + ",\"available\":" + v.isAvailable() + "}");
    }

    private static String customers(String method, Map<String, String> p) {
        if (method.equals("POST")) {
            service.addCustomer(new Customer(required(p, "id"), required(p, "name"), required(p, "phone")));
        }
        return json(service.getCustomers(), c ->
                "{\"id\":" + q(c.getCustomerId()) + ",\"name\":" + q(c.getName())
                        + ",\"phone\":" + q(c.getPhone()) + "}");
    }

    private static String quote(String method, Map<String, String> p) {
        double total = service.quote(required(p, "vehicleId"), Integer.parseInt(required(p, "days")));
        return "{\"total\":" + total + "}";
    }

    private static String rent(String method, Map<String, String> p) {
        Rental r = service.rent(required(p, "vehicleId"), required(p, "customerId"),
                Integer.parseInt(required(p, "days")));
        return "{\"message\":" + q("Rental #" + r.getRentalId() + " created. Total rent: " + r.getTotalRent()) + "}";
    }

    private static String returnVehicle(String method, Map<String, String> p) {
        Rental r = service.returnVehicle(Integer.parseInt(required(p, "rentalId")));
        return "{\"message\":" + q("Rental #" + r.getRentalId() + " returned. Vehicle is available again.") + "}";
    }

    private static String rentals(String method, Map<String, String> p) {
        return json(service.getRentals(), r ->
                "{\"id\":" + r.getRentalId() + ",\"customerId\":" + q(r.getCustomer().getCustomerId()) + ",\"customer\":" + q(r.getCustomer().getName())
                        + ",\"vehicle\":" + q(r.getVehicle().getType() + " - " + r.getVehicle().getBrand()
                        + " (" + r.getVehicle().getVehicleId() + ")")
                        + ",\"days\":" + r.getDays() + ",\"total\":" + r.getTotalRent()
                        + ",\"date\":" + q(r.getStartDate().toString())
                        + ",\"returned\":" + r.isReturned() + "}");
    }

    // ---------- helpers ----------

    private static void api(HttpExchange ex, ApiHandler handler) throws IOException {
        int status = 200;
        String body;
        try {
            Map<String, String> params = new HashMap<>();
            if (ex.getRequestMethod().equals("POST")) {
                String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                params = parseForm(raw);
            }
            body = handler.handle(ex.getRequestMethod(), params);
        } catch (NumberFormatException e) {
            status = 400;
            body = "{\"error\":" + q("Please enter valid numbers") + "}";
        } catch (IllegalArgumentException e) {
            status = 400;
            body = "{\"error\":" + q(e.getMessage()) + "}";
        }
        send(ex, status, "application/json", body.getBytes(StandardCharsets.UTF_8));
    }

    private static void serveStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/vehicles.html";
        if (path.contains("..")) { send(ex, 403, "text/plain", "Forbidden".getBytes()); return; }

        byte[] data = readWebFile(path.substring(1));
        if (data == null) { send(ex, 404, "text/plain", "Not found".getBytes()); return; }

        String type = path.endsWith(".css") ? "text/css"
                : path.endsWith(".js") ? "application/javascript" : "text/html";
        send(ex, 200, type + "; charset=utf-8", data);
    }

    // Reads from the project's src/web folder first (so edits show on refresh), then from the compiled output.
    private static byte[] readWebFile(String name) throws IOException {
        for (String base : new String[]{"src/web/", "VehicleRental/src/web/"}) {
            Path p = Path.of(base + name);
            if (Files.isRegularFile(p)) return Files.readAllBytes(p);
        }
        try (InputStream in = Main.class.getResourceAsStream("/web/" + name)) {
            if (in != null) return in.readAllBytes();
        }
        return null;
    }

    private static void send(HttpExchange ex, int status, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.sendResponseHeaders(status, data.length);
        ex.getResponseBody().write(data);
        ex.close();
    }

    private static Map<String, String> parseForm(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8).trim() : "");
        }
        return map;
    }

    private static String required(Map<String, String> p, String key) {
        String v = p.get(key);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Please fill in all fields");
        return v;
    }

    private static <T> String json(List<T> list, Function<T, String> f) {
        return "[" + list.stream().map(f).collect(Collectors.joining(",")) + "]";
    }

    private static String q(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\"";
    }

    private interface ApiHandler {
        String handle(String method, Map<String, String> params);
    }
}
