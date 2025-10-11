package org.smartMart.bootstrap;

import org.smartMart.domain.activity.Activity;
import org.smartMart.domain.catalog.Product;
import org.smartMart.domain.promo.Promotion;
import org.smartMart.indexing.CatalogIndex;
import org.smartMart.repo.inmemory.InMemoryCatalogRepo;
import org.smartMart.repo.inmemory.InMemoryPromotionRepo;
import org.smartMart.repo.inmemory.InMemoryRankingRepo;
import org.smartMart.services.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class SmartMartRunner {
    public static void main(String[] args) {
        var catalogRepo = new InMemoryCatalogRepo();
        var index = new CatalogIndex();
        var catalogSvc = new CatalogService(catalogRepo, index, 3);

        catalogSvc.upSert(new Product("SKU-1","Pixel 9", new BigDecimal("64999"), Set.of("electronics","mobile")));
        catalogSvc.upSert(new Product("SKU-2","Galaxy S24", new BigDecimal("79999"), Set.of("electronics","mobile")));
        catalogSvc.upSert(new Product("SKU-3","AirPods Pro", new BigDecimal("24999"), Set.of("electronics","audio")));
        catalogSvc.upSert(new Product("SKU-4","Kindle", new BigDecimal("12999"), Set.of("electronics","ereader")));


        System.out.println("By tag #mobile: " + catalogSvc.searchByTag("mobile"));
        System.out.println("By price 20k–70k: " + catalogSvc.searchByPrice(new BigDecimal("20000"), new BigDecimal("70000")));

        var cartSvc = new CartService(catalogRepo);
        cartSvc.add("SKU-1", 1);
        cartSvc.add("SKU-3", 2);
        System.out.println("Cart lines: " + cartSvc.view());
        System.out.println("Total: ₹" + cartSvc.total());


        var promoRepo = new InMemoryPromotionRepo();
        var promoSvc = new PromotionService(promoRepo);
        promoSvc.add(new Promotion("LAUNCH_OFFER", 1, Instant.now()));
        promoSvc.add(new Promotion("WEEKEND_SALE", 2, Instant.now()));
        System.out.println("Top promo: " + promoSvc.topActive(Instant.now()).orElse(null));

        var rankRepo = new InMemoryRankingRepo();
        var rankSvc = new RankingService(rankRepo);
        rankSvc.upsert("SKU-3", 1200);
        rankSvc.upsert("SKU-2", 900);
        rankSvc.upsert("SKU-1", 1500);
        System.out.println("Bestsellers: " + rankSvc.topN(10));


        var activitySvc = new ActivityService(5);

        activitySvc.record(new Activity("u1","VIEW:SKU-1", Instant.now()));
        activitySvc.record(new Activity("u1","ADD_TO_CART:SKU-1", Instant.now()));
        activitySvc.record(new Activity("u1","VIEW:SKU-3", Instant.now()));
        System.out.println("Recent activities: " + activitySvc.latest());
        System.out.println("Audit trail (ordered unique): " + activitySvc.audit());
    }
}
