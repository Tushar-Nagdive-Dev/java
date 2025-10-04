# 🧩 **14️⃣ HashMap — Internal Working (Deep + Visual + Enterprise Context)**

---

## 🎯 **Objective**

By the end, you’ll understand:

* The internal **bucket-based architecture**
* How **hashing**, **collisions**, and **rehashing** work
* The transition from **linked list → red-black tree** (Java 8+)
* How **load factor**, **capacity**, and **threshold** affect performance
* Enterprise-grade applications and best practices

---

## 🧠 **1. What is a HashMap?**

> `HashMap` is a **hash-table-based**, **key-value** data structure that stores entries using **hashing** for fast lookup and retrieval.

**Class Declaration:**

```java
public class HashMap<K,V> extends AbstractMap<K,V>
        implements Map<K,V>, Cloneable, Serializable
```

✅ Allows **one null key** and **multiple null values**
✅ Not thread-safe
✅ Provides O(1) average time complexity for operations

---

## ⚙️ **2. Internal Structure**

Internally, HashMap stores an **array of buckets**, where each bucket is a **linked list or a tree** of `Node<K,V>` objects.

```java
transient Node<K,V>[] table; // bucket array
transient int size;
final float loadFactor;
int threshold; // resize trigger = capacity * loadFactor
```

### Node Structure:

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next; // linked-list reference
}
```

---

### 🧩 Visualization (Initial Structure)

```
table (buckets)
index:  0   1   2   3   4   5   6   7   ... 15
        [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]
```

When you add:

```java
map.put("Tushar", 101);
```

→ The key is hashed, mapped to an index, and stored in a bucket.

---

## ⚙️ **3. The Hashing Process**

HashMap uses a **two-step process** to locate the correct bucket.

### Step 1️⃣: Compute Hash

```java
int h = key.hashCode();
h = h ^ (h >>> 16);
```

* `hashCode()` gives the base hash.
* The XOR mixing (`h ^ (h >>> 16)`) spreads entropy to avoid clustering.

### Step 2️⃣: Compute Index

```java
index = (table.length - 1) & h;
```

This bitwise AND ensures even distribution within array length (power of 2).

---

### Example:

Let’s say table size = 16, and:

```
"Tushar".hashCode() = 122345678
Binary (last 4 bits): 1110
index = 1110 = 14
```

→ Entry stored in bucket 14.

---

## ⚙️ **4. How `put()` Works (Step-by-Step)**

```java
public V put(K key, V value) {
    int hash = hash(key);
    int index = (n - 1) & hash;

    Node<K,V> first = table[index];
    for (Node<K,V> e = first; e != null; e = e.next) {
        if (e.hash == hash && (e.key == key || key.equals(e.key))) {
            e.value = value; // overwrite
            return oldValue;
        }
    }
    addNode(hash, key, value, index);
}
```

### Step-by-step:

1️⃣ Calculate hash and index.
2️⃣ Check if bucket empty → create new node.
3️⃣ If occupied → compare keys (collision check).
4️⃣ If match → replace value.
5️⃣ If different → append to linked list.

---

### 🧩 Visualization

```
index 2 → [key=101, value=“A”]
index 5 → [key=202, value=“B”] → [key=303, value=“C”]
```

When collisions occur, entries are chained.

---

## ⚙️ **5. Collision Resolution — Linked List & Tree Nodes**

Before Java 8:

* Collisions handled by **linked list chaining**.
* Worst case (all hashes same) → O(n) search.

Since Java 8:

* If bucket size > **TREEIFY_THRESHOLD (8)** → converts to **Red-Black Tree**.
* Search becomes **O(log n)** in heavily-collided buckets.

---

### 🧩 Treeification Visualization

Before:

```
index 7 → A → B → C → D → E → F → G → H → I  (Linked list)
```

After:

```
index 7 → (Red-Black Tree)
             [D]
            /   \
          [B]   [H]
```

✅ Faster lookups under collision-heavy conditions.

---

## ⚙️ **6. How `get()` Works**

```java
public V get(Object key) {
    int hash = hash(key);
    Node<K,V> e = table[(n - 1) & hash];
    for (; e != null; e = e.next) {
        if (e.hash == hash && (key == e.key || key.equals(e.key)))
            return e.value;
    }
    return null;
}
```

### Process:

1️⃣ Compute hash → locate bucket.
2️⃣ Traverse nodes → compare key with `.equals()`.
3️⃣ Return value if found, else null.

Average = O(1), Worst = O(log n) (tree).

---

## ⚙️ **7. Rehashing and Resizing**

### When?

When size exceeds `threshold = capacity × loadFactor`.

Default:

```
capacity = 16
loadFactor = 0.75
threshold = 12
```

After 12th insertion → **resize** (capacity doubled).

### How?

1️⃣ Create new table (size × 2).
2️⃣ Recalculate index for all keys.
3️⃣ Move (rehash) entries.

💡 **Why not simple copy?** Because index depends on `(n - 1) & hash`, which changes after resize.

---

## 🧱 **8. Load Factor & Performance**

| Load Factor    | Effect                               |
| :------------- | :----------------------------------- |
| Low (<0.5)     | Fewer collisions, more memory use    |
| Default (0.75) | Balanced performance                 |
| High (>0.9)    | Compact but increases collision risk |

✅ For enterprise-grade caches, **0.7–0.8** is ideal.

---

## ⚡ **9. Fail-Fast Iterator**

If you modify a map structurally (add/remove) while iterating:

```java
for (var e : map.entrySet())
    map.put("New", "Data"); // ❌ ConcurrentModificationException
```

💡 Internally uses a `modCount` tracker to detect concurrent modifications.

---

## 📊 **10. Time Complexity**

| Operation   | Average | Worst Case |
| :---------- | :-----: | :--------: |
| `put()`     |   O(1)  |  O(log n)  |
| `get()`     |   O(1)  |  O(log n)  |
| `remove()`  |   O(1)  |  O(log n)  |
| `iteration` |   O(n)  |    O(n)    |

---

## 🏢 **11. Real-World Enterprise Use Cases**

| Use Case                              | Why HashMap                  |
| :------------------------------------ | :--------------------------- |
| **Cache / Session Store**             | Constant-time key lookup     |
| **Routing Tables / Service Registry** | Fast ID → Object mapping     |
| **Configuration Properties**          | Key-value structure          |
| **Metadata Repository**               | Flexible schema-less storage |
| **ETL Pipeline Stage Lookup**         | Fast transformation mapping  |

### Example:

```java
Map<String, Integer> sessionMap = new HashMap<>();
sessionMap.put("User:Tushar", 1001);
sessionMap.put("User:Maya", 1002);

System.out.println(sessionMap.get("User:Tushar")); // 1001
```

---

## ⚠️ **12. Common Pitfalls**

| Mistake                       | Effect                      | Fix                                   |
| :---------------------------- | :-------------------------- | :------------------------------------ |
| Mutable key objects           | Lookup fails after mutation | Use immutable keys                    |
| Poor hashCode()               | Too many collisions         | Always override `hashCode()` properly |
| Large HashMaps without tuning | Memory overhead             | Adjust loadFactor/capacity            |
| Expecting thread-safety       | Race conditions             | Use `ConcurrentHashMap`               |

---

## ✅ **13. Best Practices**

✔ Use **immutable keys** (e.g., String, UUID).
✔ Always override `equals()` & `hashCode()` together.
✔ Pre-size with estimated count to avoid rehashing:

```java
new HashMap<>(expectedSize * 4 / 3);
```

✔ Don’t use for concurrent writes — prefer `ConcurrentHashMap`.
✔ Use `.computeIfAbsent()` or `.merge()` for atomic operations.

---

## 🧠 **14. Architect’s Perspective**

| Dimension              | Insight                                 |
| :--------------------- | :-------------------------------------- |
| **Design intent**      | High-speed, in-memory key-value store   |
| **Data structure**     | Array of buckets (LinkedList + Tree)    |
| **Performance goal**   | O(1) average lookup                     |
| **Concurrency**        | None (non-thread-safe)                  |
| **Trade-offs**         | Rehashing cost, memory overhead         |
| **Enterprise analogy** | Fast routing cache or configuration map |

---

## ✅ **15. Summary Table**

| Property              | HashMap                 |
| :-------------------- | :---------------------- |
| Thread-safe           | ❌                       |
| Allows null key/value | ✅                       |
| Ordered               | ❌                       |
| Backed by             | Array + LinkedList/Tree |
| Time complexity       | O(1) average            |
| Load factor           | 0.75 (default)          |
| Capacity resize       | ×2                      |
| Duplicates            | ❌ (by key)              |
| Introduced in         | Java 1.2                |

---

# 🧩 **15️⃣ LinkedHashMap — Internal Working (Deep + Visual + Enterprise Context)**

---

## 🎯 **Objective**

You’ll learn:

* How `LinkedHashMap` maintains **insertion** and **access** order
* Its internal structure (hash table + doubly-linked list)
* The difference between **insertion-order** and **access-order** modes
* How LRU caches are built using it
* Real enterprise use cases and performance considerations

---

## 🧠 **1. What is a LinkedHashMap?**

> `LinkedHashMap` is an **ordered HashMap** — it preserves the order in which keys are inserted (or accessed, depending on mode).

**Class Declaration:**

```java
public class LinkedHashMap<K,V> extends HashMap<K,V>
        implements Map<K,V>
```

✅ Inherits all HashMap behavior (hashing, O(1) performance)
✅ Adds **predictable iteration order** via a **doubly-linked list**
✅ Supports **LRU cache behavior** when access-order mode is enabled

---

## ⚙️ **2. Internal Structure**

Internally, it’s just a **HashMap** with one enhancement —
each node in the bucket table is linked via a **before/after chain**.

```java
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after;
}
```

So every key-value pair is both:

* Part of a **hash bucket** (for lookup)
* And part of a **linked list** (for order)

---

### 🧩 Visualization

#### Insertion Order Example

```
Linked List (before/after chain):
[A] <-> [B] <-> [C]

Hash Buckets (by hash):
0:[B]   1:[A]   2:[C]
```

Even though stored in different hash buckets, the **iteration** order follows `[A, B, C]` (insertion order).

---

## ⚙️ **3. How It Maintains Order**

`LinkedHashMap` overrides `HashMap.newNode()` to link nodes together:

```java
Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
    LinkedHashMap.Entry<K,V> e =
        new LinkedHashMap.Entry<>(hash, key, value, next);
    linkNodeLast(e);
    return e;
}

void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
    LinkedHashMap.Entry<K,V> last = tail;
    tail = p;
    if (last == null)
        head = p;
    else {
        p.before = last;
        last.after = p;
    }
}
```

So each insertion:
1️⃣ Adds to the hash bucket
2️⃣ Also adds at **end of doubly-linked list**

---

## ⚙️ **4. Order Modes**

| Mode                | Description                             | Constructor                                              |
| :------------------ | :-------------------------------------- | :------------------------------------------------------- |
| **Insertion order** | Default; order = order of key insertion | `new LinkedHashMap<>()`                                  |
| **Access order**    | Moves accessed keys to end              | `new LinkedHashMap<>(initialCapacity, loadFactor, true)` |

---

### 🧩 Example: Insertion Order

```java
Map<String, Integer> map = new LinkedHashMap<>();
map.put("A", 1);
map.put("B", 2);
map.put("C", 3);
System.out.println(map); // {A=1, B=2, C=3}
```

### 🧩 Example: Access Order

```java
Map<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
map.put("A", 1);
map.put("B", 2);
map.put("C", 3);
map.get("A");
System.out.println(map.keySet()); // [B, C, A]
```

✅ Accessing `A` moved it to the end — essential for **LRU caching**.

---

## ⚙️ **5. How Access Order Works Internally**

Whenever you call `get()` or `put()` (for an existing key):

```java
void afterNodeAccess(Node<K,V> e) {
    if (accessOrder && tail != e) {
        unlink(e);           // remove from current position
        linkNodeLast(e);     // move to tail
    }
}
```

💡 The `accessOrder` flag controls whether nodes move upon access.

---

## ⚙️ **6. LRU Cache Behavior**

You can override `removeEldestEntry()` to automatically remove oldest entries:

```java
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return size() > 100;
}
```

✅ This creates an **auto-evicting cache** — the most recently accessed items stay, older ones get removed.

---

### 🧩 Visualization: LRU Cache Example

```
Capacity = 3
Insert → [A, B, C]
Access “A” → moves to end → [B, C, A]
Insert “D” → exceeds capacity → removes “B”
Final order → [C, A, D]
```

---

## 📊 **7. Time Complexity**

| Operation   | Average | Explanation                          |
| :---------- | :-----: | :----------------------------------- |
| `put()`     |   O(1)  | HashMap insertion + linked list link |
| `get()`     |   O(1)  | HashMap lookup                       |
| `remove()`  |   O(1)  | HashMap removal + unlink from list   |
| `iteration` |   O(n)  | Maintains order                      |

Performance is nearly identical to HashMap — with a **tiny overhead** for the linked list.

---

## ⚡ **8. Comparison vs HashMap**

| Feature      | HashMap                 | LinkedHashMap                                |
| :----------- | :---------------------- | :------------------------------------------- |
| Order        | Unordered               | Ordered                                      |
| Backing      | Array + LinkedList/Tree | Array + LinkedList/Tree + Double Linked List |
| Performance  | Slightly faster         | Slight overhead                              |
| Use case     | Fast lookups            | Ordered caching, activity logs               |
| LRU Cache    | ❌                       | ✅ (via `removeEldestEntry`)                  |
| Access Order | ❌                       | ✅                                            |

---

## 🧱 **9. Fail-Fast Behavior**

Like `HashMap`, iterators are **fail-fast** — modifying the map while iterating throws `ConcurrentModificationException`.

---

## 🏢 **10. Real-World Enterprise Use Cases**

| Use Case                         | Why LinkedHashMap                               |
| :------------------------------- | :---------------------------------------------- |
| **LRU Caches**                   | Automatically removes least recently used items |
| **Session Management**           | Maintains last-accessed sessions                |
| **API Request Caching**          | Predictable eviction and insertion order        |
| **Audit / Event Tracking**       | Maintains sequence of events                    |
| **Pre-fetch or Hot Cache Layer** | Maintains MRU ordering                          |

---

### 🧠 Example: LRU Cache Implementation

```java
class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private final int capacity;
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > capacity;
    }
}

LRUCache<Integer, String> cache = new LRUCache<>(3);
cache.put(1, "A");
cache.put(2, "B");
cache.put(3, "C");
cache.get(1); // access 1
cache.put(4, "D"); // removes least recently used (2)
System.out.println(cache.keySet()); // [3, 1, 4]
```

✅ Perfect LRU behavior — used internally in caching libraries like Ehcache and Spring’s `@Cacheable`.

---

## ⚠️ **11. Common Pitfalls**

| Mistake                                    | Effect                        | Fix                                |
| :----------------------------------------- | :---------------------------- | :--------------------------------- |
| Assuming sorted order                      | Ordered ≠ Sorted              | Use `TreeMap` for sorting          |
| Using mutable keys                         | Hash/ordering breaks          | Use immutable keys                 |
| Overriding `removeEldestEntry` incorrectly | Cache eviction fails          | Always `return size() > capacity;` |
| Forgetting accessOrder=true                | Cache won't reorder on access | Use correct constructor            |

---

## ✅ **12. Best Practices**

✔ Use **accessOrder=true** for LRU or MRU behavior.
✔ Extend LinkedHashMap for custom caching logic.
✔ Use immutable keys for consistent hashing.
✔ Avoid extremely large maps — iteration is O(n).
✔ Wrap in `Collections.synchronizedMap()` if multithreaded.

---

## 🧠 **13. Architect’s Perspective**

| Dimension              | Insight                                             |
| :--------------------- | :-------------------------------------------------- |
| **Design intent**      | Maintain predictable order + fast access            |
| **Data structure**     | Hash table + doubly linked list                     |
| **Performance**        | O(1) operations with minimal overhead               |
| **Concurrency**        | None — use synchronized wrapper if needed           |
| **Trade-offs**         | Slight memory + pointer overhead                    |
| **Enterprise analogy** | Ordered, auto-evicting LRU cache or request tracker |

---

## ✅ **14. Summary Table**

| Property         | LinkedHashMap                |
| :--------------- | :--------------------------- |
| Thread-safe      | ❌                            |
| Ordering         | ✅ (Insertion / Access)       |
| Null keys/values | ✅                            |
| Backed by        | HashMap + Doubly Linked List |
| Duplicates       | ❌ (unique keys)              |
| Load factor      | 0.75                         |
| Time complexity  | O(1) avg                     |
| LRU Support      | ✅ via override               |
| Introduced in    | Java 1.4                     |

---

# 🧩 **16️⃣ TreeMap — Internal Working (Deep + Visual + Enterprise Context)**

---

## 🎯 **Objective**

You’ll learn:

* How `TreeMap` maintains **sorted order** using **Red–Black Tree**
* How `Comparable` and `Comparator` define order
* How range operations (`subMap`, `headMap`, `tailMap`) work
* Time complexities (O(log n)) and balancing logic
* Real-world enterprise use cases like leaderboards, price registries, and version tracking

---

## 🧠 **1. What is a TreeMap?**

> `TreeMap` is a **Red–Black Tree–based** implementation of the `NavigableMap` interface, storing keys in **sorted order**.

**Class Declaration:**

```java
public class TreeMap<K,V> extends AbstractMap<K,V>
        implements NavigableMap<K,V>, Cloneable, Serializable
```

✅ Keys sorted in **natural order** or by **custom comparator**
✅ Logarithmic time for insertion, removal, and lookup
✅ Ordered iteration guaranteed
❌ Not thread-safe
❌ Does not allow null keys (since ordering logic uses comparisons)

---

## ⚙️ **2. Internal Structure**

`TreeMap` is internally a **self-balancing binary search tree (Red–Black Tree)**.

Each node looks like this:

```java
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color; // RED or BLACK
}
```

### 🧩 Visualization

```
          (50)
         /    \
      (20)    (70)
      /  \     /
    (10) (30)(60)
```

✅ Left < Parent < Right
✅ Balanced via **coloring + rotations**

---

## ⚙️ **3. Red–Black Tree Basics**

TreeMap uses a **Red–Black Tree** to stay balanced.

### Rules:

1️⃣ Every node is red or black.
2️⃣ Root is always black.
3️⃣ Red nodes cannot have red children.
4️⃣ Every path from root to leaf has the same number of black nodes.

When these rules break after insertion/deletion, **rotations** and **recoloring** fix balance.

### Visualization (Color Example)

```
      40(B)
      /   \
  20(R)   60(R)
```

Insert 10 → violates Red-Red → Tree rotates + recolors.

✅ Balancing ensures height ≈ log₂(n).

---

## ⚙️ **4. How `put()` Works Internally**

```java
public V put(K key, V value) {
    if (root == null) {
        root = new Entry<>(key, value, null);
        root.color = BLACK;
        size = 1;
        return null;
    }
    Entry<K,V> t = root;
    Entry<K,V> parent;
    int cmp;
    do {
        parent = t;
        cmp = compare(key, t.key);
        if (cmp < 0)
            t = t.left;
        else if (cmp > 0)
            t = t.right;
        else
            return t.setValue(value); // replace existing
    } while (t != null);
    Entry<K,V> e = new Entry<>(key, value, parent);
    if (cmp < 0) parent.left = e; else parent.right = e;
    fixAfterInsertion(e); // re-balance tree
    size++;
    return null;
}
```

💡 **Key point:** Insertion order doesn’t matter — only *sorted order* matters.

---

## ⚙️ **5. Ordering Rules**

| Type                  | How Order is Defined        | Example                           |
| :-------------------- | :-------------------------- | :-------------------------------- |
| **Natural Ordering**  | Keys implement `Comparable` | `Integer`, `String`, etc.         |
| **Custom Comparator** | Passed at construction      | `Comparator.reverseOrder()`, etc. |

### Example:

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(20, "B");
map.put(10, "A");
map.put(30, "C");
System.out.println(map.keySet()); // [10, 20, 30]
```

### Custom Comparator:

```java
TreeMap<String, Integer> map = new TreeMap<>((a,b) -> b.compareTo(a));
map.put("A",1); map.put("B",2); map.put("C",3);
System.out.println(map.keySet()); // [C, B, A]
```

---

## ⚙️ **6. Range Navigation Methods (NavigableMap)**

`TreeMap` extends `NavigableMap`, enabling **range queries** and **navigation**.

| Method           | Description              | Example |
| :--------------- | :----------------------- | :------ |
| `firstKey()`     | Smallest key             |         |
| `lastKey()`      | Largest key              |         |
| `higherKey(K k)` | Next greater key         |         |
| `lowerKey(K k)`  | Next smaller key         |         |
| `subMap(a,b)`    | Keys between `a` and `b` |         |
| `headMap(k)`     | All keys < k             |         |
| `tailMap(k)`     | All keys ≥ k             |         |

### Example:

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(10, "A");
map.put(20, "B");
map.put(30, "C");

System.out.println(map.headMap(30));   // {10=A, 20=B}
System.out.println(map.tailMap(20));   // {20=B, 30=C}
System.out.println(map.subMap(10,30)); // {10=A, 20=B}
```

✅ Excellent for range searches — a major advantage over HashMap.

---

## ⚙️ **7. Deletion (`remove()`) Process**

When removing:
1️⃣ Locate node.
2️⃣ If has two children → find successor (smallest node in right subtree).
3️⃣ Replace target with successor.
4️⃣ Rebalance tree via **fixAfterDeletion()**.

⚙️ Self-balancing ensures search/insert/remove remain **O(log n)**.

---

## 📊 **8. Time Complexity**

| Operation   | Time Complexity | Notes                      |
| :---------- | :-------------: | :------------------------- |
| `put()`     |     O(log n)    | Tree insertion + balancing |
| `get()`     |     O(log n)    | Tree search                |
| `remove()`  |     O(log n)    | Tree rebalancing           |
| `iteration` |       O(n)      | In sorted order            |

---

## ⚡ **9. Comparison with Other Maps**

| Feature         | **HashMap**        | **LinkedHashMap**        | **TreeMap**       |
| :-------------- | :----------------- | :----------------------- | :---------------- |
| Order           | Unordered          | Insertion / Access order | Sorted            |
| Time (avg)      | O(1)               | O(1)                     | O(log n)          |
| Allows null key | ✅                  | ✅                        | ❌                 |
| Thread-safe     | ❌                  | ❌                        | ❌                 |
| Backed by       | Array + LinkedList | Hash + LinkedList        | Red–Black Tree    |
| Best for        | Fast lookup        | Ordered cache            | Sorted/range data |

---

## 🧱 **10. Fail-Fast Iterators**

Like HashMap, iteration during modification causes:

```
ConcurrentModificationException
```

✅ Thread-safe alternatives exist: `ConcurrentSkipListMap` (we’ll cover next).

---

## 🏢 **11. Real-World Enterprise Use Cases**

| Use Case                     | Why TreeMap                               |
| :--------------------------- | :---------------------------------------- |
| **Leaderboards / Rankings**  | Sorted order by score                     |
| **Stock/Price Registry**     | Continuous price range queries            |
| **Time-Series / Event Logs** | Natural chronological order               |
| **Rule Engines**             | Ordered priority rules                    |
| **Version Trackers**         | Natural sorting of versions or timestamps |

---

### Example: Leaderboard System

```java
class Player implements Comparable<Player> {
    String name;
    int score;
    public Player(String name, int score) {
        this.name = name; this.score = score;
    }
    public int compareTo(Player p) {
        return Integer.compare(p.score, score); // descending
    }
    public String toString() { return name + ":" + score; }
}

TreeMap<Player, String> leaderboard = new TreeMap<>();
leaderboard.put(new Player("Maya", 95), "");
leaderboard.put(new Player("Tushar", 92), "");
leaderboard.put(new Player("Riya", 97), "");

System.out.println(leaderboard.keySet()); // [Riya:97, Maya:95, Tushar:92]
```

✅ Perfect for sorted scoreboards, rankers, and priority systems.

---

## ⚠️ **12. Common Pitfalls**

| Mistake                   | Effect                 | Fix                                         |
| :------------------------ | :--------------------- | :------------------------------------------ |
| Using null key            | `NullPointerException` | Use non-null keys                           |
| Mutable keys              | Tree order breaks      | Use immutable keys                          |
| Inconsistent comparator   | Data corruption        | Ensure comparator consistency               |
| Large maps without tuning | High balancing cost    | For large data, use `ConcurrentSkipListMap` |

---

## ✅ **13. Best Practices**

✔ Use immutable, comparable keys.
✔ For descending order, use:

```java
new TreeMap<>(Comparator.reverseOrder());
```

✔ For range lookups, prefer `subMap()` or `tailMap()`.
✔ Avoid frequent bulk mutations — rebalancing is expensive.
✔ For concurrent scenarios, use **ConcurrentSkipListMap**.

---

## 🧠 **14. Architect’s Perspective**

| Dimension              | Insight                                              |
| :--------------------- | :--------------------------------------------------- |
| **Design intent**      | Sorted, navigable, balanced key-value storage        |
| **Data structure**     | Red–Black Tree                                       |
| **Concurrency**        | Single-threaded only                                 |
| **Performance**        | O(log n) with guaranteed order                       |
| **Enterprise analogy** | Ranking system, rule registry, time-indexed database |

---

## ✅ **15. Summary Table**

| Property         | TreeMap         |
| :--------------- | :-------------- |
| Thread-safe      | ❌               |
| Sorted           | ✅               |
| Allows null key  | ❌               |
| Backed by        | Red–Black Tree  |
| Time complexity  | O(log n)        |
| Duplicates       | ❌ (unique keys) |
| Load factor      | N/A             |
| Range operations | ✅               |
| Introduced in    | Java 1.2        |

---

# 🧩 **17️⃣ ConcurrentHashMap — Internal Working (Deep + Visual + Enterprise Context)**

---

## 🎯 **Objective**

By the end, you’ll understand:

* How `ConcurrentHashMap` achieves **thread safety without global locks**
* How it evolved from **segmented locking (Java 7)** to **CAS-based fine-grained locking (Java 8+)**
* How it performs **non-blocking reads, concurrent writes, and safe iteration**
* Key internal classes (`Node`, `TreeBin`, `CounterCell`)
* Real-world use cases like distributed caches, API registries, and metrics collectors

---

## 🧠 **1. What is a ConcurrentHashMap?**

> A **thread-safe** HashMap designed for **high concurrency** and **non-blocking reads**, without the performance penalties of synchronizing the entire map.

**Class Declaration:**

```java
public class ConcurrentHashMap<K,V>
        extends AbstractMap<K,V>
        implements ConcurrentMap<K,V>, Serializable
```

✅ Thread-safe
✅ Highly scalable
✅ Supports atomic operations (`putIfAbsent`, `compute`, `merge`)
✅ Fail-safe iteration
❌ Allows null neither for key nor for value

---

## ⚙️ **2. Internal Structure (Java 8+)**

Internally, it’s a **hash-table-like array of Nodes**, but without global locks.

```java
transient volatile Node<K,V>[] table;
```

Each **bucket (bin)** may contain:

* A single Node (linked list)
* A TreeBin (red-black tree)
* A ForwardingNode (temporary during resizing)

### Node Structure

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    volatile V val;
    volatile Node<K,V> next;
}
```

✅ Volatile fields → visibility across threads
✅ CAS (Compare-And-Set) used to update pointers safely

---

## 🧱 **3. How Concurrency Was Improved (From Java 7 to 8)**

| Version     | Concurrency Mechanism         | Description                                                        |
| :---------- | :---------------------------- | :----------------------------------------------------------------- |
| **Java 7**  | Segmented Locking             | Map divided into 16–32 segments, each with its own lock.           |
| **Java 8+** | Fine-grained CAS + Node Locks | Each bucket/node can be locked independently; reads are lock-free. |

💡 Result: Up to 100× higher throughput under multi-threaded load.

---

## ⚙️ **4. Put Operation (Deep Flow)**

### Simplified Source

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent) {
    for (;;) {
        Node<K,V>[] tab = table;
        int i = (n - 1) & hash;
        Node<K,V> f = tabAt(tab, i);
        if (f == null) {
            if (casTabAt(tab, i, null, new Node<>(hash, key, value, null)))
                break; // inserted atomically
        }
        else if (f.hash == MOVED)
            tab = helpTransfer(tab, f); // assist resize
        else {
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    Node<K,V> e = f;
                    for (;;){
                        if (e.hash == hash && key.equals(e.key)) {
                            e.val = value; // replace
                            break;
                        }
                        if (e.next == null) { e.next = new Node<>(hash,key,value,null); break; }
                        e = e.next;
                    }
                }
            }
            break;
        }
    }
    addCount(1L, binCount);
    return null;
}
```

### 🔍 Step-by-step

1️⃣ Compute hash and bucket index.
2️⃣ If bucket empty → **CAS** insert new node.
3️⃣ If occupied → lock that bucket only (`synchronized(f)`).
4️⃣ If collision → append node or treeify if too large.
5️⃣ Update count atomically.

✅ Multiple threads can write to different buckets simultaneously.

---

## ⚙️ **5. Get Operation (Non-Blocking)**

```java
public V get(Object key) {
    Node<K,V>[] tab = table;
    int i = (n - 1) & hash(key);
    Node<K,V> e = tabAt(tab, i);
    while (e != null) {
        if (e.hash == hash && key.equals(e.key))
            return e.val;
        e = e.next;
    }
    return null;
}
```

💡 No locks for reads — relies on **volatile visibility** and immutable links.

✅ Safe because:

* Insertions use CAS
* Deletions nullify values first
* Iteration never blocks

---

## ⚙️ **6. Treeification (When Buckets Grow Too Large)**

If a bucket contains > 8 entries (`TREEIFY_THRESHOLD`):

* Converts linked list → balanced Red-Black Tree (`TreeBin`)
* Search within bucket drops to O(log n)

If resize in progress or capacity < 64 → expands instead of treeifying.

---

## ⚙️ **7. Resizing (Concurrent Transfer)**

When map load factor exceeds 0.75:

* New table created (double size)
* Threads **cooperate** to transfer buckets (`helpTransfer()`)
* Old buckets replaced by `ForwardingNode` placeholders

✅ No global lock
✅ Resizing doesn’t block reads or writes

---

## 🧱 **8. Atomic Methods**

| Method                   | Behavior                                          |
| :----------------------- | :------------------------------------------------ |
| `putIfAbsent(K,V)`       | Insert only if key not present                    |
| `remove(K,V)`            | Remove only if key/value match                    |
| `replace(K,V)`           | Replace if present                                |
| `compute()`, `merge()`   | Atomic read-modify-write                          |
| `forEach()` & `reduce()` | Parallel functional operations using ForkJoinPool |

💡 These methods are critical for safe updates in multi-threaded apps without external synchronization.

---

## 📊 **9. Time Complexity**

| Operation     | Average |   Worst  |
| :------------ | :-----: | :------: |
| `get()`       |   O(1)  | O(log n) |
| `put()`       |   O(1)  | O(log n) |
| `remove()`    |   O(1)  | O(log n) |
| `iteration()` |   O(n)  |   O(n)   |

---

## ⚡ **10. Comparison vs Other Maps**

| Feature     | **HashMap**          | **ConcurrentHashMap** | **Collections.synchronizedMap** |
| :---------- | :------------------- | :-------------------- | :------------------------------ |
| Thread-safe | ❌                    | ✅                     | ✅                               |
| Concurrency | None                 | High                  | Poor (global lock)              |
| Locks       | —                    | Bucket-level (CAS)    | Entire map                      |
| Null keys   | ✅                    | ❌                     | ✅                               |
| Iterators   | Fail-fast            | Fail-safe             | Fail-fast                       |
| Performance | Fast single-threaded | Scales under load     | Bottlenecked                    |

---

## 🧱 **11. Fail-Safe Iterator**

Iteration snapshot is **weakly consistent**:

* Reflects state of map at some point during iteration.
* Doesn’t throw `ConcurrentModificationException`.
* May miss recent changes, but never throws or loops forever.

---

## 🏢 **12. Real-World Enterprise Use Cases**

| Use Case                                       | Why ConcurrentHashMap                  |
| :--------------------------------------------- | :------------------------------------- |
| **Distributed Cache (L2 Cache, Spring Cache)** | Concurrent access without locks        |
| **Microservice Registry**                      | Safe read/write of service metadata    |
| **Metrics Collector**                          | Many writers updating shared stats map |
| **Real-Time Analytics Store**                  | Continuous aggregation of events       |
| **Security Session Registry**                  | Track active sessions across threads   |

---

### Example — Real-Time Request Counter

```java
ConcurrentHashMap<String, AtomicInteger> counter = new ConcurrentHashMap<>();

void recordRequest(String api) {
    counter.computeIfAbsent(api, k -> new AtomicInteger(0)).incrementAndGet();
}
```

✅ Completely thread-safe
✅ No explicit locks
✅ Efficient for thousands of concurrent updates

---

## ⚠️ **13. Common Pitfalls**

| Mistake                                | Problem                      | Fix                                    |
| :------------------------------------- | :--------------------------- | :------------------------------------- |
| Using null keys/values                 | `NullPointerException`       | Avoid nulls                            |
| Assuming strongly consistent iteration | May skip updates             | Use locks if strong consistency needed |
| Manual synchronization                 | Redundant, hurts performance | Avoid external synchronized            |
| Modifying objects used as keys         | Hash instability             | Use immutable keys                     |
| Using compute/merge improperly         | Infinite loops on recursion  | Use atomic lambda logic only           |

---

## ✅ **14. Best Practices**

✔ Use **immutable keys** like String, UUID.
✔ Leverage atomic methods (`computeIfAbsent`, `merge`) for aggregations.
✔ Avoid global locks or synchronized blocks around it.
✔ Tune initial capacity to reduce resizing.
✔ Use for read-heavy or mixed read/write workloads — not write-only.

---

## 🧠 **15. Architect’s Perspective**

| Dimension              | Insight                                          |
| :--------------------- | :----------------------------------------------- |
| **Design goal**        | High-throughput concurrent Map                   |
| **Data structure**     | Bucket array + fine-grained locks + CAS          |
| **Concurrency model**  | Lock-striping + non-blocking reads               |
| **Performance**        | Near O(1) with linear scaling                    |
| **Trade-offs**         | Slight memory overhead for concurrency           |
| **Enterprise analogy** | Shared state registry or real-time metrics cache |

---

## ✅ **16. Summary Table**

| Property          | ConcurrentHashMap                     |
| :---------------- | :------------------------------------ |
| Thread-safe       | ✅                                     |
| Null keys/values  | ❌                                     |
| Concurrency       | High (Non-blocking reads)             |
| Backed by         | Hash table + CAS nodes                |
| Fail-safe         | ✅                                     |
| Treeify threshold | 8                                     |
| Load factor       | 0.75                                  |
| Time complexity   | O(1) avg                              |
| Introduced in     | Java 1.5 (major revamp Java 8)        |
| Use case          | Concurrent cache, registry, analytics |

---

# 🧩 **18️⃣ WeakHashMap — Internal Working (Deep + Visual + Enterprise Context)**

---

## 🎯 **Objective**

By the end, you’ll understand:

* How `WeakHashMap` works with **Garbage Collection (GC)**
* The concept of **weak references** and how they differ from strong references
* How entries are automatically removed when keys are no longer in use
* Its internal structure, cleanup mechanism, and real-world usage patterns
* Enterprise examples like caching and object lifecycle management

---

## 🧠 **1. What is a WeakHashMap?**

> `WeakHashMap` is a **HashMap-like data structure** where **keys are stored as weak references**, allowing automatic removal of entries when keys are no longer referenced elsewhere.

**Class Declaration:**

```java
public class WeakHashMap<K,V> extends AbstractMap<K,V>
        implements Map<K,V>
```

✅ Automatically removes stale entries
✅ Great for memory-sensitive caches
❌ Not thread-safe (wrap with synchronization if needed)

---

## ⚙️ **2. The Core Idea: Weak References**

### 🧩 Types of References in Java

| Type                  |       GC Collectible?       | Example Use                  |
| :-------------------- | :-------------------------: | :--------------------------- |
| **Strong Reference**  |             ❌ No            | `Object obj = new Object();` |
| **Soft Reference**    |     ✅ Only if memory low    | Caching large objects        |
| **Weak Reference**    | ✅ As soon as no strong refs | Used in `WeakHashMap`        |
| **Phantom Reference** |     ✅ After finalization    | Low-level cleanup hooks      |

💡 Weak references **do not prevent GC** — once the key object is no longer strongly referenced, GC reclaims it.

---

## ⚙️ **3. Internal Structure**

Internally similar to `HashMap`, but keys are wrapped in `WeakReference` objects linked to a `ReferenceQueue`.

```java
private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
transient Entry<K,V>[] table;

private static class Entry<K,V> extends WeakReference<Object>
        implements Map.Entry<K,V> {
    final int hash;
    V value;
    Entry<K,V> next;
}
```

* Each key = weak reference
* Each Entry registered with the `ReferenceQueue`
* When GC clears a key → it’s enqueued into `queue` for cleanup

---

### 🧠 Visualization

```
WeakHashMap<Key, Value> table:
[ WeakRef(A) -> Value1 ]
[ WeakRef(B) -> Value2 ]
[ WeakRef(C) -> Value3 ]

Heap:
A, B, C → referenced by app
```

If the app drops reference to `B`:

```
B becomes unreachable → GC collects → enqueues WeakRef(B)
Cleanup thread removes entry [B->Value2]
```

✅ Memory automatically reclaimed.

---

## ⚙️ **4. Cleanup Process**

Before every major operation (put, get, remove), the map performs a **cleanup sweep**:

```java
private void expungeStaleEntries() {
    Reference<?> ref;
    while ((ref = queue.poll()) != null) {
        Entry<?,?> e = (Entry<?,?>) ref;
        int i = indexFor(e.hash, table.length);
        Entry<K,V> prev = table[i];
        Entry<K,V> p = prev;
        while (p != null) {
            Entry<K,V> next = p.next;
            if (p == e) {
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                break;
            }
            prev = p;
            p = next;
        }
    }
}
```

💡 So, stale entries are purged lazily — not in real time but during next access.

---

## ⚙️ **5. How `put()` Works**

```java
public V put(K key, V value) {
    expungeStaleEntries();        // clean up old keys
    int hash = hash(key);
    int i = indexFor(hash, table.length);
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        if (e.hash == hash && key.equals(e.get())) {
            e.value = value;      // replace
            return;
        }
    }
    table[i] = new Entry<>(key, value, queue, hash, table[i]);
    size++;
}
```

* Key is wrapped in a `WeakReference`.
* When GC collects key → entry removed automatically.

---

## ⚙️ **6. How `get()` Works**

```java
public V get(Object key) {
    expungeStaleEntries();
    int hash = hash(key);
    int i = indexFor(hash, table.length);
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        Object k = e.get(); // dereference WeakReference
        if (k == key || (k != null && k.equals(key)))
            return e.value;
    }
    return null;
}
```

💡 If GC already cleared the key → `e.get()` returns null → entry is skipped.

---

## ⚙️ **7. Behavior Example**

```java
Map<Object, String> map = new WeakHashMap<>();
Object key1 = new Object();
Object key2 = new Object();

map.put(key1, "A");
map.put(key2, "B");

System.out.println(map); // {key1=A, key2=B}

key1 = null;
System.gc(); // GC clears key1

Thread.sleep(1000);
System.out.println(map); // {key2=B} ✅ key1 entry removed
```

✅ Automatic memory cleanup!
✅ No manual removal needed.

---

## 📊 **8. Time Complexity**

| Operation     | Average | Notes                         |
| :------------ | :-----: | :---------------------------- |
| `put()`       |   O(1)  | Same as HashMap               |
| `get()`       |   O(1)  | Slight overhead for cleanup   |
| `remove()`    |   O(1)  | Fast                          |
| `iteration()` |   O(n)  | Automatically skips GC’d keys |

---

## ⚡ **9. Difference vs HashMap**

| Feature       | **HashMap**              | **WeakHashMap**             |
| :------------ | :----------------------- | :-------------------------- |
| Key reference | Strong                   | Weak                        |
| GC behavior   | Keys prevent GC          | Keys GC’d when unreferenced |
| Use case      | Normal key-value storage | Memory-sensitive cache      |
| Thread-safe   | ❌                        | ❌                           |
| Allows null   | ✅                        | ✅                           |
| Ordering      | No                       | No                          |

---

## 🧱 **10. Comparison with Other Maps**

| Map               | Thread-safe | Auto-clean | Ordering   | Use Case                     |
| :---------------- | :---------- | :--------- | :--------- | :--------------------------- |
| HashMap           | ❌           | ❌          | ❌          | Regular storage              |
| LinkedHashMap     | ❌           | ❌          | ✅          | Ordered storage or LRU cache |
| WeakHashMap       | ❌           | ✅          | ❌          | Memory-sensitive cache       |
| ConcurrentHashMap | ✅           | ❌          | ❌          | Multi-threaded map           |
| TreeMap           | ❌           | ❌          | ✅ (Sorted) | Ordered data                 |

---

## 🏢 **11. Real-World Enterprise Use Cases**

| Use Case                               | Why WeakHashMap                               |
| :------------------------------------- | :-------------------------------------------- |
| **Object Caching (auto-expiring)**     | Automatically clears unused keys              |
| **ClassLoader-based Cache**            | Frees memory when classes unloaded            |
| **Metadata Store in ORMs (Hibernate)** | Avoids memory leaks when entities GC’d        |
| **Component Context Maps**             | Keeps weak refs to short-lived objects        |
| **Session or Token Cache**             | Automatically expires when key reference lost |

---

### 🧠 Example: Auto-expiring Cache

```java
Map<UserSession, UserData> sessionCache = new WeakHashMap<>();

void putSession(UserSession s, UserData d) {
    sessionCache.put(s, d);
}

// When session object no longer referenced → entry auto-removed
```

✅ Keeps memory footprint under control
✅ Prevents leaks in large-scale web applications

---

## ⚠️ **12. Common Pitfalls**

| Mistake                            | Problem                      | Fix                                      |
| :--------------------------------- | :--------------------------- | :--------------------------------------- |
| Expecting deterministic cleanup    | GC timing unpredictable      | Don’t rely on timing                     |
| Using weak keys with primitives    | Keys not GC’d                | Use object wrappers                      |
| Holding strong reference elsewhere | Entry never collected        | Ensure keys aren’t referenced            |
| Using for critical data            | Data may vanish unexpectedly | Use strong references or explicit caches |

---

## ✅ **13. Best Practices**

✔ Use **for cache keys**, not critical data.
✔ Avoid manually triggering `System.gc()` — let JVM handle it.
✔ Do not store keys with long-lived references.
✔ For concurrent access, wrap it:

```java
Map<K,V> safeCache = Collections.synchronizedMap(new WeakHashMap<>());
```

✔ Combine with SoftReference for smart memory-sensitive caching.

---

## 🧠 **14. Architect’s Perspective**

| Dimension              | Insight                                        |
| :--------------------- | :--------------------------------------------- |
| **Design goal**        | Memory-efficient caching                       |
| **Data structure**     | HashMap + WeakReference + ReferenceQueue       |
| **Performance**        | Slightly slower than HashMap due to GC         |
| **Concurrency**        | Single-threaded (can wrap)                     |
| **Trade-offs**         | Automatic GC cleanup, but unpredictable timing |
| **Enterprise analogy** | Auto-cleaning in-memory metadata registry      |

---

## ✅ **15. Summary Table**

| Property        | WeakHashMap              |
| :-------------- | :----------------------- |
| Thread-safe     | ❌                        |
| Auto-cleaning   | ✅                        |
| Key reference   | WeakReference            |
| Nulls allowed   | ✅                        |
| Backed by       | HashMap + ReferenceQueue |
| Time complexity | O(1) avg                 |
| GC behavior     | Entries auto-removed     |
| Use case        | Memory-aware cache       |
| Introduced in   | Java 1.2                 |

---

# 🧩 **19️⃣ IdentityHashMap — Internal Working (Deep + Visual + Enterprise Context)**

---

## 🎯 **Objective**

By the end, you’ll understand:

* How `IdentityHashMap` compares keys using **`==` instead of `.equals()`**
* Its internal design and array-based structure (not buckets like HashMap)
* Why it’s faster but non-standard in key comparison
* How it’s used internally in **object graph traversal, serialization, and dependency injection frameworks**

---

## 🧠 **1. What is an IdentityHashMap?**

> `IdentityHashMap` is a **Map implementation** that compares keys and values using **reference equality (`==`)** rather than **logical equality (`.equals()`)**.

**Class Declaration:**

```java
public class IdentityHashMap<K,V>
        extends AbstractMap<K,V>
        implements Map<K,V>, Serializable, Cloneable
```

✅ Keys are considered equal **only if they are the exact same object in memory**.
✅ Allows **null** keys and values.
✅ Not synchronized (single-threaded or wrap with `Collections.synchronizedMap()`).

---

## ⚙️ **2. Key Difference from HashMap**

| Feature        | **HashMap**      | **IdentityHashMap**                  |
| :------------- | :--------------- | :----------------------------------- |
| Key comparison | `.equals()`      | `==`                                 |
| Hash code      | `key.hashCode()` | `System.identityHashCode(key)`       |
| Use case       | Logical equality | Reference identity                   |
| Performance    | Balanced         | Faster (no equals/hashCode overhead) |
| Thread-safe    | ❌                | ❌                                    |
| Null keys      | ✅ (one)          | ✅ (multiple, treated by reference)   |

💡 `IdentityHashMap` is for scenarios where *logical equality is wrong* — i.e., when you care about **object identity, not content**.

---

## ⚙️ **3. Internal Data Structure**

Unlike `HashMap`’s array of buckets, `IdentityHashMap` uses a **flat array of alternating key/value pairs**.

```java
transient Object[] table; // [key1, value1, key2, value2, ...]
```

Example:

```
[ key1 | val1 | key2 | val2 | key3 | val3 | ... ]
```

💡 Fast direct index access — no Node objects, no chaining.

---

### 🧩 Visualization

```
|0|1|2|3|4|5|6|7|8|9|...
|K1|V1|K2|V2|K3|V3|K4|V4|
```

Keys and values alternate — index for key `i`: `2*i`, value: `2*i+1`.

---

## ⚙️ **4. How Key Comparison Works**

### In `HashMap`

```java
if (key1.equals(key2)) { ... }
```

### In `IdentityHashMap`

```java
if (key1 == key2) { ... }
```

No `.equals()` invocation — purely **memory address equality**.

### Hashing

Uses:

```java
System.identityHashCode(key)
```

→ Based on object’s identity, not value.

---

## ⚙️ **5. How `put()` Works (Simplified)**

```java
public V put(K key, V value) {
    int hash = System.identityHashCode(key);
    int index = (hash & (table.length - 1)) << 1;
    for (;;) {
        Object k = table[index];
        if (k == key) { // same object
            table[index + 1] = value;
            return oldValue;
        }
        if (k == null) {
            table[index] = key;
            table[index + 1] = value;
            size++;
            return null;
        }
        index = (index + 2) % table.length; // linear probe
    }
}
```

### Key points:

* **Open addressing** used for collision resolution (no chaining).
* Linear probing ensures compact memory layout and high cache locality.

---

### 🧩 Visualization

```
Table (capacity 8):
Index: 0  1  2  3  4  5  6  7
        K1 V1 K2 V2 K3 V3  -  -
```

If collision → moves to next free pair (linear probe).

---

## ⚙️ **6. How `get()` Works**

```java
public V get(Object key) {
    int hash = System.identityHashCode(key);
    int index = (hash & (table.length - 1)) << 1;
    for (;;) {
        Object k = table[index];
        if (k == key)
            return (V) table[index + 1];
        if (k == null)
            return null;
        index = (index + 2) % table.length;
    }
}
```

💡 Still O(1) average, O(n) worst (if collisions extreme).

---

## ⚙️ **7. How It Handles Collisions**

No linked nodes or trees.
Uses **open addressing with linear probing**, similar to how `ThreadLocalMap` works.

Example:

```
index=2 occupied → try index=4 → then 6 → then wrap to 0
```

Efficient for low load factors (< 0.7).

---

## ⚙️ **8. Behavior Example**

```java
IdentityHashMap<String, String> map = new IdentityHashMap<>();

String k1 = new String("Maya");
String k2 = new String("Maya");

map.put(k1, "A");
map.put(k2, "B");

System.out.println(map.size()); // 2 ✅ (different objects)
```

👉 Even though both have same content, they are **different objects** (different memory addresses).

Now compare with HashMap:

```java
Map<String,String> map2 = new HashMap<>();
map2.put(k1, "A");
map2.put(k2, "B");
System.out.println(map2.size()); // 1 ❌ (equals() sees same string)
```

---

## ⚙️ **9. Null Handling**

Unlike HashMap:

* Multiple null keys allowed — but only if they are **different reference instances of null** (internally treated as a special placeholder `NULL_KEY`).
* Values can be null freely.

---

## 📊 **10. Time Complexity**

| Operation     | Average | Notes                |
| :------------ | :-----: | :------------------- |
| `put()`       |   O(1)  | Array + linear probe |
| `get()`       |   O(1)  | Open addressing      |
| `remove()`    |   O(1)  | Same                 |
| `iteration()` |   O(n)  | Array traversal      |

💡 Fastest key/value map under single-threaded conditions due to **flat array memory model**.

---

## ⚡ **11. Comparison vs HashMap**

| Feature        | **HashMap**         | **IdentityHashMap**         |
| :------------- | :------------------ | :-------------------------- |
| Key comparison | `.equals()`         | `==`                        |
| Hash           | `hashCode()`        | `System.identityHashCode()` |
| Structure      | Buckets + Nodes     | Flat array                  |
| Collisions     | Linked list or tree | Open addressing             |
| Order          | Unordered           | Unordered                   |
| Nulls          | One key             | Multiple                    |
| Thread-safe    | ❌                   | ❌                           |

---

## 🧱 **12. Enterprise-Level Use Cases**

| Use Case                                          | Why IdentityHashMap                                     |
| :------------------------------------------------ | :------------------------------------------------------ |
| **Serialization & Object Graph Tracking**         | Detects already visited objects by reference, not value |
| **Reflection Frameworks (e.g., Spring, Jackson)** | Tracks processed objects to avoid infinite recursion    |
| **Dependency Injection Containers**               | Maps bean instances (same object identity)              |
| **Caching Object Metadata**                       | Keeps per-instance metadata without equality conflicts  |
| **JVM / GC Internals**                            | Tracks object identity relationships                    |

---

### Example: Object Graph Traversal

```java
void traverse(Object obj, IdentityHashMap<Object, Boolean> visited) {
    if (visited.containsKey(obj)) return; // already processed
    visited.put(obj, true);
    // process fields recursively...
}
```

✅ Prevents infinite recursion when same object appears in multiple branches.

---

## ⚠️ **13. Common Pitfalls**

| Mistake                             | Problem                                | Fix                         |
| :---------------------------------- | :------------------------------------- | :-------------------------- |
| Expecting logical equality          | Stores duplicates                      | Use HashMap                 |
| Comparing different wrapper objects | Multiple entries for same value        | Use `.equals()` based map   |
| Assuming predictable iteration      | Order undefined                        | Use LinkedHashMap if needed |
| Using mutable keys                  | No effect (compares by reference only) | Fine, but avoid confusion   |
| Expecting thread safety             | Race conditions                        | Use synchronized wrapper    |

---

## ✅ **14. Best Practices**

✔ Use when **object identity** matters more than equality (framework-level).
✔ Never use for domain data (users, transactions, etc.).
✔ Keep load factor ≤ 0.75 for better probing performance.
✔ Wrap using `Collections.synchronizedMap()` if multi-threaded.
✔ Avoid combining with GC-sensitive keys — prefer WeakHashMap for that.

---

## 🧠 **15. Architect’s Perspective**

| Dimension              | Insight                                                   |
| :--------------------- | :-------------------------------------------------------- |
| **Design goal**        | Ultra-fast, reference-based Map for framework-level usage |
| **Data structure**     | Flat array, open addressing                               |
| **Performance**        | Extremely fast in single-threaded workloads               |
| **Concurrency**        | None                                                      |
| **Trade-offs**         | Ignores equals/hashCode, unordered                        |
| **Enterprise analogy** | Object graph or dependency tracking registry              |

---

## ✅ **16. Summary Table**

| Property        | IdentityHashMap               |
| :-------------- | :---------------------------- |
| Thread-safe     | ❌                             |
| Key comparison  | `==` (reference equality)     |
| Null keys       | ✅                             |
| Backed by       | Flat array (key-value pairs)  |
| Collisions      | Linear probe                  |
| Time complexity | O(1) avg                      |
| Use case        | Serialization, reflection, DI |
| Introduced in   | Java 1.4                      |

---

# 🧱 **Map Hierarchy — Complete Summary**

| Map Type              | Order            | Thread-safe | Key Comparison | GC-aware | Primary Use              |
| :-------------------- | :--------------- | :---------- | :------------- | :------- | :----------------------- |
| **HashMap**           | Unordered        | ❌           | `.equals()`    | ❌        | General-purpose          |
| **LinkedHashMap**     | Insertion/Access | ❌           | `.equals()`    | ❌        | Caches (LRU)             |
| **TreeMap**           | Sorted           | ❌           | `.compareTo()` | ❌        | Sorted registries        |
| **WeakHashMap**       | Unordered        | ❌           | `.equals()`    | ✅        | Auto-clean caches        |
| **ConcurrentHashMap** | Unordered        | ✅           | `.equals()`    | ❌        | Thread-safe cache        |
| **IdentityHashMap**   | Unordered        | ❌           | `==`           | ❌        | Object identity tracking |

---
