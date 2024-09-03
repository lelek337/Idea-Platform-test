package com.example.ideaplatform.controller;

import com.example.ideaplatform.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/min-flight-times")
    public String getMinFlightTimes() {
        return ticketService.getMinFlightTimesText();
    }

    @GetMapping("/price-stats")
    public String getPriceStats() {
        return ticketService.getPriceStatsText();
    }
}