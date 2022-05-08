package com.journi.challenge.repositories;

import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseStats;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Named
@Singleton
public class PurchasesRepository {

    private final List<Purchase> allPurchases = new ArrayList<>();

    public List<Purchase> list() {
        return allPurchases;
    }

    public void save(Purchase purchase) {
        allPurchases.add(purchase);
    }

    public PurchaseStats getLast30DaysStats() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));

        LocalDateTime start = LocalDate.now().atStartOfDay().minusDays(30);

        final Double[] minMaxAndSum = {Double.MAX_VALUE, Double.MIN_VALUE,0.0d};
        List<Purchase> recentPurchases = allPurchases
                .stream()
                .filter(p -> p.getTimestamp().isAfter(start))
                .sorted(Comparator.comparing(Purchase::getTimestamp)).
                collect(Collectors.toList());


        recentPurchases.forEach(purchase -> {
            Double currentValue = purchase.getTotalValue();
            minMaxAndSum[0] = Math.min(minMaxAndSum[0], currentValue);
            minMaxAndSum[1] = Math.max(minMaxAndSum[1], currentValue);
            minMaxAndSum[2]+=currentValue;
        });

        long countPurchases = recentPurchases.size();
        double totalAmountPurchases = minMaxAndSum[2];
        double minValue = (minMaxAndSum[0] == Double.MAX_VALUE ? 0.0 : minMaxAndSum[0]);
        double maxValue = (minMaxAndSum[1] == Double.MIN_VALUE ? 0.0 : minMaxAndSum[1]);
        // It might happen,
        // that recentPurchases is empty for the last 30 days, so directly accessing list index would fail, now defaults to null for those cases
        LocalDateTime fromDate = recentPurchases.size() > 0 ?  recentPurchases.get(0).getTimestamp() : null;
        LocalDateTime toDate =  recentPurchases.size() > 0 ?  recentPurchases.get(recentPurchases.size() - 1).getTimestamp() : null;
        return new PurchaseStats(
                Objects.nonNull(fromDate) ? formatter.format(fromDate) : null,
                Objects.nonNull(toDate) ? formatter.format(toDate) : null,
                countPurchases,
                totalAmountPurchases,
                //countPurchases might be 0, to so avoid divide by Zero error, handled for it here !
                totalAmountPurchases / (countPurchases == 0L ? 1L : countPurchases),
                minValue,
                maxValue
        );
    }
}
