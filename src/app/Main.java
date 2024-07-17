package app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Scanner;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import com.google.gson.Gson;

public class Main {

    public static URI getRelativePath(String path) {
        String url = "https://v6.exchangerate-api.com/v6/c082de3399579efec9c5fe5a/" + path;
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            System.out.println("Error al crear URI");
            return null;
        }
    }
    
    /* Para convertir el json a un mapa de Java */
    public static Map<String, String> convertJson(String json) {
        Gson gson = new Gson();
        Type stringType = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(json, stringType);
    }
    
    public static String getConversionRates(String json) {
        int divices = json.indexOf("\"conversion_rates\":") + 19;
        int end = json.length() - 2;
        return json.substring(divices, end);
    }
    
    public static void printCurrencies(Map<String, String> currencies) {
        int counter = 0;
        for(String key : currencies.keySet()) {
            System.out.print(key + "\t");
            counter++;
            
            if(counter >= 15) {
                System.out.println("");
                counter = 0;
            }
        }
        System.out.println("");
    }
    
    public static String requestCurrencies(URI path) {
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                        .uri(path)
                        .GET()
                        .build();
            HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
            return res.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static double askCurrencyToUser(Scanner scanner, Map<String, String> currencies) {
        while (true) {
            if (!scanner.hasNextLine()) {
                System.out.println("No se detectó entrada. Por favor, ingrese una moneda válida:");
                continue;
            }
    
            String userSelection = scanner.nextLine().toUpperCase();
    
            if (userSelection.isEmpty()) {
                System.out.println("La entrada vacía no es válida. Por favor, ingrese una moneda:");
                continue;
            }
    
            String currency = currencies.get(userSelection);
            if (currency != null) {
                return Double.parseDouble(currency);
            } else {
                System.out.println("Esa moneda no está en la selección, seleccione otra:");
            }
        }
    }
    
    public static void main(String[] args) {
        try (Scanner scan = new Scanner(System.in)) {
            String jsonResponse = "";
            URI url = getRelativePath("latest/USD");
            jsonResponse = requestCurrencies(url);
    
            String conversions = getConversionRates(jsonResponse);
            Map<String, String> currencies = convertJson(conversions);
    
            printCurrencies(currencies);
    
            System.out.println("Seleccione una de estas monedas como moneda base:");
            double base = askCurrencyToUser(scan, currencies);
    
            System.out.println("¿Cuánto dinero desea convertir?");
            while (!scan.hasNextDouble()) {
                System.out.println("Por favor, ingrese un número válido.");
                scan.next();  // Descarta la entrada no válida
            }
            double money = scan.nextDouble();
    
            System.out.println("Seleccione una de estas monedas como moneda objetivo:");
            double target = askCurrencyToUser(scan, currencies);
    
            double conversion = (money / base) * target;
    
            System.out.println("Resultado de la conversión: " + conversion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
