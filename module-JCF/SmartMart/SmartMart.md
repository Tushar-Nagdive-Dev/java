# 🛒 Project: **SmartMart — In-Memory Retail Engine (Collections-First)**

## Goal

Model a realistic e-commerce core (catalog, search/indexing, cart, pricing, caching, recent activity, ranking, promotions) using the **right collection for the job**, with clean domain models and production-like structure. We’ll keep it single-threaded now; in the next phase we’ll add executors, locks, and concurrent collections.

---

## High-Level Modules & the Collection Chosen

| Module            | Purpose                  | Primary Collections (why)                                                                         |
| ----------------- | ------------------------ | ------------------------------------------------------------------------------------------------- |
| `CatalogStore`    | Master store of products | `HashMap<String, Product>` for O(1) by SKU                                                        |
| `CatalogIndex`    | Secondary indexes        | `TreeMap<BigDecimal, Set<String>>` for price range; `HashMap<String, Set<String>>` for tag → SKUs |
| `LRUCache<K,V>`   | Hot cache for products   | `LinkedHashMap<K,V>` with `accessOrder=true` and `removeEldestEntry`                              |
| `PromotionEngine` | Priority of promos       | `PriorityQueue<Promotion>` by start time/priority                                                 |
| `BestsellerRank`  | Top sellers              | `TreeSet<SalesStat>` with custom comparator (sorted)                                              |
| `RecentActivity`  | Per-user recent actions  | `ArrayDeque<Activity>` for fast head/tail ops                                                     |
| `Deduper`         | One-day unique views     | `HashSet<String>` (e.g., composed keys user+sku+day)                                              |
| `AuditTrail`      | Ordered unique events    | `LinkedHashSet<String>` (stable iteration, no dups)                                               |
| `Cart`            | Line items with quantity | `LinkedHashMap<String, CartLine>` (stable order, quick replace)                                   |

> In the concurrency phase, we’ll swap/augment with `ConcurrentHashMap`, `ConcurrentLinkedQueue`, `LinkedBlockingQueue`, and schedulers.

---

## Domain Models (hash/equals done right)

```java
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

final class Product {
    final String sku;                // immutable identity
    final String title;
    final BigDecimal price;
    final Set<String> tags;          // e.g., ["electronics","mobile"]

    Product(String sku, String title, BigDecimal price, Set<String> tags) {
        this.sku = Objects.requireNonNull(sku);
        this.title = Objects.requireNonNull(title);
        this.price = Objects.requireNonNull(price);
        this.tags = Set.copyOf(tags); // immutable defensive copy
    }
    @Override public boolean equals(Object o) {
        return (o instanceof Product p) && sku.equals(p.sku); // identity by SKU
    }
    @Override public int hashCode() { return sku.hashCode(); }
    @Override public String toString() { return sku + " " + title + " ₹" + price; }
}

final class Promotion {
    final String id;
    final int priority;             // lower = higher priority
    final Instant startAt;
    Promotion(String id, int priority, Instant startAt) {
        this.id = id; this.priority = priority; this.startAt = startAt;
    }
    @Override public String toString(){ return id + "(p=" + priority + ")"; }
}

final class SalesStat implements Comparable<SalesStat> {
    final String sku; final long units;
    SalesStat(String sku, long units) { this.sku = sku; this.units = units; }
    public int compareTo(SalesStat o) {
        int byUnits = Long.compare(o.units, this.units); // desc
        return byUnits != 0 ? byUnits : this.sku.compareTo(o.sku);
    }
    @Override public String toString(){ return sku + ":" + units; }
}

record CartLine(String sku, int qty) { }
record Activity(String userId, String action, Instant at) { }
```

---

## Core Stores & Indexes

```java
final class CatalogStore {
    private final Map<String, Product> bySku = new HashMap<>();
    public void upsert(Product p) { bySku.put(p.sku, p); }
    public Product get(String sku) { return bySku.get(sku); }
    public Collection<Product> all() { return bySku.values(); }
}

final class CatalogIndex {
    private final NavigableMap<BigDecimal, Set<String>> priceToSkus = new TreeMap<>();
    private final Map<String, Set<String>> tagToSkus = new HashMap<>();

    public void index(Product p) {
        priceToSkus.computeIfAbsent(p.price, k -> new HashSet<>()).add(p.sku);
        for (String t : p.tags)
            tagToSkus.computeIfAbsent(t, k -> new HashSet<>()).add(p.sku);
    }
    public void remove(Product p) {
        Optional.ofNullable(priceToSkus.get(p.price)).ifPresent(s -> s.remove(p.sku));
        for (String t: p.tags) Optional.ofNullable(tagToSkus.get(t)).ifPresent(s -> s.remove(p.sku));
    }
    public Set<String> findByPriceRange(BigDecimal min, BigDecimal max) {
        Set<String> out = new LinkedHashSet<>();
        priceToSkus.subMap(min, true, max, true).values().forEach(out::addAll);
        return out;
    }
    public Set<String> findByTag(String tag) {
        return tagToSkus.getOrDefault(tag, Set.of());
    }
}
```

---

## LRU Cache (hot product cache)

```java
class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private final int capacity;
    LRUCache(int capacity) { super(capacity, 0.75f, true); this.capacity = capacity; }
    @Override protected boolean removeEldestEntry(Map.Entry<K,V> e) { return size() > capacity; }
}
```

---

## Promotions (priority queue)

```java
final class PromotionEngine {
    private final PriorityQueue<Promotion> q =
      new PriorityQueue<>(Comparator
        .comparingInt((Promotion p) -> p.priority)
        .thenComparing(p -> p.startAt));

    public void add(Promotion p){ q.add(p); }
    public Promotion peekActive(Instant now){ return q.peek(); } // simplistic
}
```

---

## Activity & Audit

```java
final class RecentActivity {
    private final Deque<Activity> deque = new ArrayDeque<>();
    private final int limit;
    RecentActivity(int limit){ this.limit = limit; }
    public void record(Activity a){
        deque.addFirst(a);
        if (deque.size() > limit) deque.removeLast();
    }
    public List<Activity> latest(){ return List.copyOf(deque); }
}

final class AuditTrail {
    private final Set<String> orderedUnique = new LinkedHashSet<>();
    public void record(String msg){ orderedUnique.add(msg); }
    public List<String> dump(){ return List.copyOf(orderedUnique); }
}
```

---

## Cart

```java
final class Cart {
    private final Map<String, CartLine> lines = new LinkedHashMap<>();
    public void add(String sku, int qty){
        lines.merge(sku, new CartLine(sku, qty),
            (oldL, newL) -> new CartLine(sku, oldL.qty() + newL.qty()));
    }
    public void remove(String sku){ lines.remove(sku); }
    public BigDecimal total(CatalogStore catalog){
        BigDecimal sum = BigDecimal.ZERO;
        for (CartLine l : lines.values()) {
            Product p = catalog.get(l.sku());
            sum = sum.add(p.price.multiply(BigDecimal.valueOf(l.qty())));
        }
        return sum;
    }
    public List<CartLine> view(){ return List.copyOf(lines.values()); }
}
```

---

## Putting It Together (Demo Flow)

```java
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class SmartMartDemo {
    public static void main(String[] args) {
        var catalog = new CatalogStore();
        var index   = new CatalogIndex();
        var cache   = new LRUCache<String, Product>(3);
        var promos  = new PromotionEngine();
        var rank    = new java.util.TreeSet<SalesStat>();
        var recent  = new RecentActivity(5);
        var audit   = new AuditTrail();
        var dedupe  = new java.util.HashSet<String>();
        var cart    = new Cart();

        // Seed catalog
        var p1 = new Product("SKU-1","Pixel 9", new BigDecimal("64999"), Set.of("electronics","mobile"));
        var p2 = new Product("SKU-2","Galaxy S24", new BigDecimal("79999"), Set.of("electronics","mobile"));
        var p3 = new Product("SKU-3","AirPods Pro", new BigDecimal("24999"), Set.of("electronics","audio"));
        var p4 = new Product("SKU-4","Kindle", new BigDecimal("12999"), Set.of("electronics","ereader"));
        for (var p : new Product[]{p1,p2,p3,p4}) { catalog.upsert(p); index.index(p); }

        // LRU cache usage
        cache.put(p1.sku, p1); cache.put(p2.sku, p2); cache.put(p3.sku, p3);
        cache.get(p1.sku); // access moves to MRU
        cache.put(p4.sku, p4); // evicts least recent (likely p2)

        // Price range, tag search
        System.out.println("₹20k–₹70k: " + index.findByPriceRange(new BigDecimal("20000"), new BigDecimal("70000")));
        System.out.println("#mobile: " + index.findByTag("mobile"));

        // Promotions by priority
        promos.add(new Promotion("LAUNCH_OFFER", 1, Instant.now()));
        promos.add(new Promotion("WEEKEND_SALE", 2, Instant.now()));
        System.out.println("Top promo: " + promos.peekActive(Instant.now()));

        // Bestsellers ranking (TreeSet sorted)
        rank.add(new SalesStat("SKU-3", 1200));
        rank.add(new SalesStat("SKU-2", 900));
        rank.add(new SalesStat("SKU-1", 1500));
        System.out.println("Bestsellers: " + rank);

        // Recent activity (ArrayDeque)
        recent.record(new Activity("u1","VIEW:SKU-1", Instant.now()));
        recent.record(new Activity("u1","ADD_TO_CART:SKU-1", Instant.now()));
        recent.record(new Activity("u1","VIEW:SKU-3", Instant.now()));
        System.out.println("Recent: " + recent.latest());

        // Deduplicate daily views
        String viewKey = "u1|SKU-1|2025-10-04";
        if (dedupe.add(viewKey)) audit.record("COUNT_VIEW " + viewKey);

        // Cart
        cart.add("SKU-1", 1);
        cart.add("SKU-3", 2);
        System.out.println("Cart lines: " + cart.view());
        System.out.println("Cart total: ₹" + cart.total(catalog));
        System.out.println("Audit (ordered unique): " + audit.dump());
    }
}
```

What this demo proves (Collections in action):

* **O(1)** SKU lookups (`HashMap`)
* **Range queries** & **tag indexes** (`TreeMap` + `HashSet`)
* **LRU** hot data (`LinkedHashMap` in access-order)
* **Priority dispatch** (`PriorityQueue`)
* **Sorted leaderboards** (`TreeSet` with comparator)
* **Recent buffers** (`ArrayDeque`)
* **De-dup** (`HashSet`)
* **Stable, unique audit log** (`LinkedHashSet`)
* **Stable cart order + quick merges** (`LinkedHashMap`)

---

## Stretch Exercises (to cement mastery)

1. **Add “similar items” index**: `Map<String, Set<String>>` SKU → related SKUs; keep **insertion order** with `LinkedHashSet`.
2. **Price buckets**: Precompute `Map<String /*bucket*/, Set<String>>` for fast faceting.
3. **Immutable views**: Expose read APIs returning `Collections.unmodifiable*`.
4. **Promotion expiry sweep**: Move expired promos to an **`IdentityHashMap<Promotion, Boolean>`** to track unique instances (identity vs equals).
5. **Weak cache**: Add a small `WeakHashMap<Product, Metadata>` to see entries vanish when products drop out of strong refs.

---

# 🧱 Project layout (Gradle • Java 17+)

```
smartmart/
├─ build.gradle
├─ settings.gradle
├─ src/
│  ├─ main/java/
│  │  └─ com/smartmart/
│  │     ├─ common/                 # primitives, Result, errors, ids
│  │     ├─ config/                 # wiring & constants
│  │     ├─ domain/                 # pure domain (no infra)
│  │     │  ├─ catalog/
│  │     │  ├─ cart/
│  │     │  ├─ promo/
│  │     │  ├─ ranking/
│  │     │  └─ activity/
│  │     ├─ ports/                  # hexagonal ports (interfaces)
│  │     ├─ services/               # application services (use-cases)
│  │     ├─ repo/                   # adapters (in-memory now)
│  │     │  ├─ inmemory/
│  │     ├─ indexing/               # indexes & search views
│  │     ├─ cache/                  # LRU, weak caches
│  │     ├─ api/                    # CLI or HTTP façade (later)
│  │     └─ bootstrap/              # demo runner / seed data
│  └─ test/java/
│     └─ com/smartmart/...
```

### Gradle (minimal)

```groovy
plugins { id 'java' }
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
repositories { mavenCentral() }
dependencies {
  testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}
test { useJUnitPlatform() }
```

---

# 🧩 Packages & responsibilities (Collections focus)

## `common`

* `Result<T>` (success/failure), `Errors`, `Ids`
* **Why**: keep services simple & side-effect free.

```java
package com.smartmart.common;
public sealed interface Result<T> {
  record Ok<T>(T value) implements Result<T> {}
  record Err<T>(String message) implements Result<T> {}
  static <T> Ok<T> ok(T v){ return new Ok<>(v); }
  static <T> Err<T> err(String m){ return new Err<>(m); }
}
```

---

## `domain.catalog`

* **Entities**: `Product` (immutable, identity = `sku`)
* **Collections**: `Set<String> tags` (immutable)

```java
package com.smartmart.domain.catalog;
import java.math.BigDecimal; import java.util.*;
public final class Product {
  public final String sku, title; public final BigDecimal price; public final Set<String> tags;
  public Product(String sku, String title, BigDecimal price, Set<String> tags){
    this.sku = Objects.requireNonNull(sku);
    this.title = Objects.requireNonNull(title);
    this.price = Objects.requireNonNull(price);
    this.tags = Set.copyOf(tags);
  }
  @Override public boolean equals(Object o){ return o instanceof Product p && sku.equals(p.sku); }
  @Override public int hashCode(){ return sku.hashCode(); }
  @Override public String toString(){ return sku+" "+title+" ₹"+price; }
}
```

## `domain.cart`

* **Aggregates**: `Cart`, `CartLine`
* **Collections**: `LinkedHashMap<String, CartLine>` to keep **stable add order** and fast merge.

```java
package com.smartmart.domain.cart;
import java.math.BigDecimal; import java.util.*;
import com.smartmart.domain.catalog.Product;
public final class Cart {
  private final Map<String, CartLine> lines = new LinkedHashMap<>();
  public void add(String sku, int qty){ lines.merge(sku, new CartLine(sku, qty),
      (a,b) -> new CartLine(sku, a.qty()+b.qty())); }
  public void remove(String sku){ lines.remove(sku); }
  public BigDecimal total(Map<String, Product> bySku){
    BigDecimal sum = BigDecimal.ZERO;
    for (CartLine l : lines.values()) sum = sum.add(bySku.get(l.sku()).price().multiply(BigDecimal.valueOf(l.qty())));
    return sum;
  }
  public List<CartLine> view(){ return List.copyOf(lines.values()); }
}
public record CartLine(String sku, int qty){}
```

## `domain.promo`

* **Model**: `Promotion` (priority, startAt)
* **Collections** (later services): `PriorityQueue<Promotion>`.

```java
package com.smartmart.domain.promo;
import java.time.Instant;
public record Promotion(String id, int priority, Instant startAt) {}
```

## `domain.ranking`

* **Model**: `SalesStat implements Comparable<SalesStat>` → **TreeSet** natural sort.

```java
package com.smartmart.domain.ranking;
public record SalesStat(String sku, long units) implements Comparable<SalesStat> {
  public int compareTo(SalesStat o){ int c = Long.compare(o.units, units); return (c!=0)?c:sku.compareTo(o.sku); }
}
```

## `domain.activity`

* **Model**: `Activity` (userId, action, timestamp) → **ArrayDeque** in service.

---

## `ports` (hexagonal)

* `CatalogPort` (read/write), `RankingPort`, `PromotionPort`, `ActivityPort`—all **interfaces**.

```java
package com.smartmart.ports;
import com.smartmart.domain.catalog.Product; import java.util.*;
public interface CatalogPort {
  void upsert(Product p);
  Optional<Product> bySku(String sku);
  Collection<Product> all();
}
```

---

## `repo.inmemory` (adapters using right collections)

### `InMemoryCatalogRepo`

* **Collections**: `HashMap<String, Product>` (O(1) lookup)

```java
package com.smartmart.repo.inmemory;
import com.smartmart.domain.catalog.*; import com.smartmart.ports.CatalogPort;
import java.util.*;
public final class InMemoryCatalogRepo implements CatalogPort {
  private final Map<String, Product> bySku = new HashMap<>();
  @Override public void upsert(Product p){ bySku.put(p.sku(), p); }
  @Override public Optional<Product> bySku(String sku){ return Optional.ofNullable(bySku.get(sku)); }
  @Override public Collection<Product> all(){ return bySku.values(); }
  // for services needing map view
  public Map<String, Product> viewMap(){ return bySku; }
}
```

### `InMemoryRankingRepo`

* **Collections**: `TreeSet<SalesStat>` (sorted)

```java
package com.smartmart.repo.inmemory;
import com.smartmart.domain.ranking.SalesStat; import java.util.*;
public final class InMemoryRankingRepo {
  private final NavigableSet<SalesStat> leaderboard = new TreeSet<>();
  public void upsert(SalesStat s){ leaderboard.remove(s); leaderboard.add(s); }
  public NavigableSet<SalesStat> topN(int n){ return leaderboard.stream().limit(n).collect(java.util.stream.Collectors.toCollection(TreeSet::new)); }
}
```

### `InMemoryPromotionRepo`

* **Collections**: `PriorityQueue<Promotion>`

```java
package com.smartmart.repo.inmemory;
import com.smartmart.domain.promo.Promotion; import java.time.Instant; import java.util.*;
public final class InMemoryPromotionRepo {
  private final PriorityQueue<Promotion> q = new PriorityQueue<>(
      Comparator.comparingInt(Promotion::priority).thenComparing(Promotion::startAt));
  public void add(Promotion p){ q.add(p); }
  public Optional<Promotion> peekActive(Instant now){ return Optional.ofNullable(q.peek()); }
}
```

---

## `indexing`

* `CatalogIndex` for price ranges & tags: **TreeMap<BigDecimal, Set<String>>** + **HashMap<String, Set<String>>**.

```java
package com.smartmart.indexing;
import com.smartmart.domain.catalog.Product; import java.math.BigDecimal; import java.util.*;
public final class CatalogIndex {
  private final NavigableMap<BigDecimal, Set<String>> priceToSkus = new TreeMap<>();
  private final Map<String, Set<String>> tagToSkus = new HashMap<>();
  public void index(Product p){
    priceToSkus.computeIfAbsent(p.price(), k -> new HashSet<>()).add(p.sku());
    for (String t : p.tags()) tagToSkus.computeIfAbsent(t, k -> new HashSet<>()).add(p.sku());
  }
  public void remove(Product p){
    Optional.ofNullable(priceToSkus.get(p.price())).ifPresent(s -> s.remove(p.sku()));
    for (String t: p.tags()) Optional.ofNullable(tagToSkus.get(t)).ifPresent(s -> s.remove(p.sku()));
  }
  public Set<String> byPriceRange(BigDecimal min, BigDecimal max){
    Set<String> out = new LinkedHashSet<>();
    priceToSkus.subMap(min, true, max, true).values().forEach(out::addAll);
    return out;
  }
  public Set<String> byTag(String tag){ return tagToSkus.getOrDefault(tag, Set.of()); }
}
```

---

## `cache`

* `LRUCache<K,V>` using **LinkedHashMap** (`accessOrder=true`)

```java
package com.smartmart.cache;
import java.util.*;
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
  private final int capacity;
  public LRUCache(int capacity){ super(capacity, 0.75f, true); this.capacity = capacity; }
  @Override protected boolean removeEldestEntry(Map.Entry<K,V> e){ return size() > capacity; }
}
```

* (Optional) `WeakMetaCache<K,Meta>` using **WeakHashMap** later.

---

## `services` (application/use-case layer)

### `CatalogService`

* Orchestrates repo + indexes + cache. Demonstrates **Collections choreography**.

```java
package com.smartmart.services;
import com.smartmart.cache.LRUCache;
import com.smartmart.domain.catalog.Product;
import com.smartmart.indexing.CatalogIndex;
import com.smartmart.ports.CatalogPort;
import java.math.BigDecimal; import java.util.*;

public final class CatalogService {
  private final CatalogPort repo;
  private final CatalogIndex index;
  private final LRUCache<String, Product> hot;

  public CatalogService(CatalogPort repo, CatalogIndex index, int hotCap){
    this.repo = repo; this.index = index; this.hot = new LRUCache<>(hotCap);
  }

  public void upsert(Product p){
    repo.upsert(p); index.index(p); hot.put(p.sku(), p);
  }

  public Optional<Product> bySku(String sku){
    Product fromHot = hot.get(sku);
    if (fromHot != null) return Optional.of(fromHot);
    Optional<Product> p = repo.bySku(sku);
    p.ifPresent(v -> hot.put(sku, v));
    return p;
  }

  public Set<String> searchByTag(String tag){ return index.byTag(tag); }
  public Set<String> searchByPrice(BigDecimal min, BigDecimal max){ return index.byPriceRange(min, max); }
}
```

### `CartService`

* Uses `LinkedHashMap` cart + repo map view.

```java
package com.smartmart.services;
import com.smartmart.domain.cart.*; import com.smartmart.repo.inmemory.InMemoryCatalogRepo;
import java.math.BigDecimal; import java.util.List;

public final class CartService {
  private final InMemoryCatalogRepo catalog;
  private final Cart cart = new Cart();
  public CartService(InMemoryCatalogRepo catalog){ this.catalog = catalog; }
  public void add(String sku, int qty){ cart.add(sku, qty); }
  public void remove(String sku){ cart.remove(sku); }
  public BigDecimal total(){ return cart.total(catalog.viewMap()); }
  public List<CartLine> view(){ return cart.view(); }
}
```

### `PromotionService`, `RankingService`, `ActivityService`

* `PromotionService` → `PriorityQueue`
* `RankingService` → `TreeSet`
* `ActivityService` → `ArrayDeque` buffer + `LinkedHashSet` audit (ordered-unique)

```java
package com.smartmart.services;
import com.smartmart.domain.activity.Activity; import java.time.Instant; import java.util.*;

public final class ActivityService {
  private final Deque<Activity> recent = new ArrayDeque<>();
  private final int limit; private final Set<String> auditOrderedUnique = new LinkedHashSet<>();
  public ActivityService(int limit){ this.limit = limit; }
  public void record(Activity a){
    recent.addFirst(a); if (recent.size() > limit) recent.removeLast();
    auditOrderedUnique.add(a.userId()+"|"+a.action()+"|"+a.at());
  }
  public List<Activity> latest(){ return List.copyOf(recent); }
  public List<String> audit(){ return List.copyOf(auditOrderedUnique); }
}
```

---

## `bootstrap` (demo runner)

```java
package com.smartmart.bootstrap;
import com.smartmart.domain.catalog.*; import com.smartmart.indexing.CatalogIndex;
import com.smartmart.repo.inmemory.*; import com.smartmart.services.*;
import java.math.BigDecimal; import java.util.Set;

public class DemoApp {
  public static void main(String[] args) {
    var catalogRepo = new InMemoryCatalogRepo();
    var index = new CatalogIndex();
    var catalogSvc = new CatalogService(catalogRepo, index, 3);
    // seed
    catalogSvc.upsert(new Product("SKU-1","Pixel 9", new BigDecimal("64999"), Set.of("electronics","mobile")));
    catalogSvc.upsert(new Product("SKU-2","Galaxy S24", new BigDecimal("79999"), Set.of("electronics","mobile")));
    catalogSvc.upsert(new Product("SKU-3","AirPods Pro", new BigDecimal("24999"), Set.of("electronics","audio")));
    // lookups
    System.out.println("By tag mobile: " + catalogSvc.searchByTag("mobile"));
    System.out.println("By price 20k–70k: " + catalogSvc.searchByPrice(new BigDecimal("20000"), new BigDecimal("70000")));
    // cart
    var cartSvc = new CartService(catalogRepo);
    cartSvc.add("SKU-1", 1); cartSvc.add("SKU-3", 2);
    System.out.println("Cart lines: " + cartSvc.view());
    System.out.println("Total: ₹" + cartSvc.total());
  }
}
```

---

# ✅ Why this structure scales (and prepares you for concurrency)

* **Hexagonal**: `ports` (interfaces) decouple domain/services from infra (`repo.inmemory`).
* **Collections at the right places** (fast maps for identity, trees for order, deques for recent, LRU for hot path).
* **Swap-ready**: Next phase you can:

    * Replace `HashMap` with `ConcurrentHashMap` in repos.
    * Introduce `LinkedBlockingQueue` for async events (orders/activities).
    * Add `ScheduledThreadPoolExecutor` for promotion refresh / cache TTL.
    * Use `CompletableFuture` to parallelize catalog + promo + pricing.

---

# 🧪 Tests (taste)

```
src/test/java/com/smartmart/services/CatalogServiceTest.java
```

```java
package com.smartmart.services;
import com.smartmart.domain.catalog.*; import com.smartmart.indexing.CatalogIndex;
import com.smartmart.repo.inmemory.InMemoryCatalogRepo;
import org.junit.jupiter.api.*; import java.math.BigDecimal; import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CatalogServiceTest {
  @Test void searchByTagAndPrice(){
    var repo = new InMemoryCatalogRepo(); var idx = new CatalogIndex();
    var svc = new CatalogService(repo, idx, 2);
    svc.upsert(new Product("A","A", new BigDecimal("100"), Set.of("x")));
    svc.upsert(new Product("B","B", new BigDecimal("200"), Set.of("x","y")));
    assertTrue(svc.searchByTag("x").containsAll(Set.of("A","B")));
    assertEquals(Set.of("B"), svc.searchByPrice(new BigDecimal("150"), new BigDecimal("250")));
  }
}
```

---

# 🚦 Implementation roadmap (next phase hooks)

1. **Eventing**: Add `orders` module and an `OrderEvent` **LinkedBlockingQueue**; worker consumes & updates `SalesStat` (`TreeSet`) and `ActivityService`.
2. **Concurrency**: switch `InMemoryCatalogRepo.bySku` to **ConcurrentHashMap**, guard indexes with **read/write** strategies.
3. **Async tasks**: `ExecutorService` to rebuild indexes after bulk upserts; `ScheduledThreadPoolExecutor` to rotate promos.
4. **Futures**: `CompletableFuture` to fetch product + promo + recommendations in parallel for PDP.
5. **Backpressure**: replace in-memory event queue with **ArrayBlockingQueue** in rate-sensitive components.
