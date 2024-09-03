package com.example.ideaplatform.service;

import com.example.ideaplatform.model.Ticket;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    private List<Ticket> tickets;

    /**
     * Загружаем и парсим JSON-файл при инициализации
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Ticket>> data = objectMapper.readValue(new File("tickets.json"),
                new TypeReference<Map<String, List<Ticket>>>() {});
        this.tickets = data.get("tickets");
    }

    public String getMinFlightTimesText() {
        List<Ticket> vvoToTlvTickets = filterTickets("VVO", "TLV");
        Map<String, Integer> minFlightTimes = calculateMinFlightTimes(vvoToTlvTickets);
        StringBuilder result = new StringBuilder("Минимальное время полёта между Владивостоком и Тель-Авивом: <br>");
        minFlightTimes.forEach((carrier, time) -> result.append(carrier)
                .append(": ").append(time/60).append(" часов ")
                .append(time % 60).append(" минут<br>"));
        return result.toString();
    }

    public String getPriceStatsText() {
        List<Ticket> vvoToTlvTickets = filterTickets("VVO", "TLV");
        double averagePrice = calculateAveragePrice(vvoToTlvTickets);
        double medianPrice = calculateMedianPrice(vvoToTlvTickets);
        double priceDifference = averagePrice - medianPrice;

        return String.format("Средняя цена: %.2f<br>Медиана цены: %.2f<br>Разница между средней ценой и медианой: %.2f",
                averagePrice, medianPrice, priceDifference);
    }

    private List<Ticket> filterTickets(String origin, String destination) {
        return tickets.stream()
                .filter(ticket -> origin.equals(ticket.getOrigin()) && destination.equals(ticket.getDestination()))
                .toList();
    }

    private Map<String, Integer> calculateMinFlightTimes(List<Ticket> tickets) {
        Map<String, Integer> minFlightTimes = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

        for (Ticket ticket : tickets) {
            if (ticket.getDepartureDate() == null || ticket.getDepartureTime() == null ||
                    ticket.getArrivalDate() == null || ticket.getArrivalTime() == null) {
                System.out.println("Неверный билет: " + ticket);
                continue; // Пропускаем этот билет
            }
            String carrier = ticket.getCarrier();
            try {
                int flightTime = calculateFlightTime(ticket, dateFormat);
                minFlightTimes.merge(carrier, flightTime, Math::min);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return minFlightTimes;
    }

    private int calculateFlightTime(Ticket ticket, SimpleDateFormat dateFormat) throws ParseException {
        Date departure = dateFormat.parse(ticket.getDepartureDate() + " " + ticket.getDepartureTime());
        Date arrival = dateFormat.parse(ticket.getArrivalDate() + " " + ticket.getArrivalTime());
                                        // Время полёта в минутах
        return (int) ((arrival.getTime() - departure.getTime()) / (1000 * 60));
    }

    private double calculateAveragePrice(List<Ticket> tickets) {
        return tickets.stream()
                .mapToInt(Ticket::getPrice)
                .average()
                .orElse(0);
    }

    private double calculateMedianPrice(List<Ticket> tickets) {
        List<Integer> prices = tickets.stream()
                .map(Ticket::getPrice)
                .sorted()
                .toList();

        int size = prices.size();
        if (size % 2 == 0) {
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            return prices.get(size / 2);
        }
    }
}