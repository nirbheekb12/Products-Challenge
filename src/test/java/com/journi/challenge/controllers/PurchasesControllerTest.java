package com.journi.challenge.controllers;

import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.PurchasesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchasesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PurchasesController purchasesController;
    @Autowired
    private PurchasesRepository purchasesRepository;



    private String getPurchaseJson(String invoiceNumber, String customerName, String dateTime, Double totalValue, String currencyCode, String... productIds) {
        String productIdList = "[\"" + String.join("\",\"", productIds) + "\"]";
        return String.format(Locale.US,"{\"invoiceNumber\":\"%s\",\"customerName\":\"%s\",\"dateTime\":\"%s\",\"productIds\":%s,\"amount\":%.2f,\"currencyCode\":\"%s\"}", invoiceNumber, customerName, dateTime, productIdList, totalValue, currencyCode);
    }

    @Test
    public void testPurchaseCurrencyCodeEUR() throws Exception {
        String body = getPurchaseJson("1", "customer 1", "2020-01-01T10:00:00+01:00", 25.34, "EUR", "product1");
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk());

        Purchase savedPurchase = purchasesRepository.list().get(purchasesRepository.list().size() - 1);
        assertEquals("customer 1", savedPurchase.getCustomerName());
        assertEquals("1", savedPurchase.getInvoiceNumber());
        assertEquals("2020-01-01T10:00:00", savedPurchase.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals(25.34, savedPurchase.getTotalValue());
    }

    @Test
    @DisplayName(value = "Purchase Input having any null value, should fail with 400")
    public void testPurchaseCurrencyCodeHavingNullValues() throws Exception {
        String body = getPurchaseJson("1", null, "2020-01-01T10:00:00+01:00", null, "EUR", "product1");
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    public void testPurchaseCurrencyCodeHavingInvalidCurrencyCode() throws Exception {
        String body = getPurchaseJson("1", "customer 1", "2020-01-01T10:00:00+01:00", 25.34, "asfasfasfas", "product1");
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().is4xxClientError());
    }


    @Test
    public void testPurchaseCurrencyCodeUS() throws Exception {
        String body = getPurchaseJson("1", "customer 1", "2020-01-01T10:00:00+01:00", 25.34, "USD", "product1");
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk());

        Purchase savedPurchase = purchasesRepository.list().get(purchasesRepository.list().size() - 1);
        assertEquals("customer 1", savedPurchase.getCustomerName());
        assertEquals("1", savedPurchase.getInvoiceNumber());
        assertEquals("2020-01-01T10:00:00", savedPurchase.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals(22.65129167784035, savedPurchase.getTotalValue());
    }

    @Test
    public void testPurchaseStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDate = now.minusDays(20);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));
        // Inside window purchases
        purchasesRepository.save(new Purchase("1", firstDate, Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(1), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(2), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(3), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(4), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(5), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(6), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(7), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(8), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(9), Collections.emptyList(), "", 10.0,"EUR"));

        // Outside window purchases
        purchasesRepository.save(new Purchase("1", now.minusDays(31), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(31), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(32), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(33), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(34), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(35), Collections.emptyList(), "", 10.0, "EUR"));

        PurchaseStats purchaseStats = purchasesController.getStats();
        assertEquals(formatter.format(firstDate), purchaseStats.getFrom());
        assertEquals(formatter.format(firstDate.plusDays(9)), purchaseStats.getTo());
        assertEquals(10, purchaseStats.getCountPurchases());
        assertEquals(100.0, purchaseStats.getTotalAmount());
        assertEquals(10.0, purchaseStats.getAvgAmount());
        assertEquals(10.0, purchaseStats.getMinAmount());
        assertEquals(10.0, purchaseStats.getMaxAmount());
    }

    @Test
    @DisplayName(value = "For no records for last 30 days, should returned appropriate response having defaulted values and not fail")
    public void testPurchaseStatisticsErrorScenario() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDate = now.minusDays(20);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));


        // Outside window purchases
        purchasesRepository.save(new Purchase("1", now.minusDays(31), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(31), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(32), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(33), Collections.emptyList(), "", 10.0, "EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(34), Collections.emptyList(), "", 10.0,"EUR"));
        purchasesRepository.save(new Purchase("1", now.minusDays(35), Collections.emptyList(), "", 10.0, "EUR"));

        PurchaseStats purchaseStats = purchasesController.getStats();
        assertEquals(null, purchaseStats.getFrom());
        assertEquals(null, purchaseStats.getTo());
        assertEquals(0, purchaseStats.getCountPurchases());
        assertEquals(0.0, purchaseStats.getTotalAmount());
        assertEquals(0.0, purchaseStats.getAvgAmount());
        assertEquals(0.0, purchaseStats.getMinAmount());
        assertEquals(0.0, purchaseStats.getMaxAmount());
    }
}
