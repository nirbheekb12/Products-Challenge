package com.journi.challenge.controllers;

import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseRequest;
import com.journi.challenge.models.PurchaseResponseDto;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.PurchasesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RestController
public class PurchasesController {

    @Inject
    private PurchasesRepository purchasesRepository;

    private CurrencyConverter currencyConverter = new CurrencyConverter();

    @GetMapping("/purchases/statistics")
    public PurchaseStats getStats() {
        return purchasesRepository.getLast30DaysStats();
    }

    @PostMapping("/purchases")
    public ResponseEntity<?> save(@RequestBody PurchaseRequest purchaseRequest) {
        String purchaseCode = currencyConverter.extractCurrencyFromCurrencyCode(purchaseRequest.getCurrencyCode());
        if (Objects.nonNull(purchaseRequest.getAmount())
                && Objects.nonNull(purchaseRequest.getCurrencyCode())
                && Objects.nonNull(purchaseRequest.getProductIds())
                && Objects.nonNull(purchaseRequest.getCustomerName())
                && Objects.nonNull(purchaseRequest.getDateTime())
                && purchaseCode != null) {
            Purchase newPurchase = new Purchase(
                    purchaseRequest.getInvoiceNumber(),
                    LocalDateTime.parse(purchaseRequest.getDateTime(), DateTimeFormatter.ISO_DATE_TIME),
                    purchaseRequest.getProductIds(),
                    purchaseRequest.getCustomerName(),
                    purchaseRequest.getAmount()
            );
            purchasesRepository.save(newPurchase);
//            return newPurchase;
                  return new ResponseEntity<>(new PurchaseResponseDto("200", "Ok. Purchase saved"), HttpStatus.OK);
        }
//        return null;

        return new ResponseEntity<>(new PurchaseResponseDto("400", "Invalid request"), HttpStatus.BAD_REQUEST);


    }
}
