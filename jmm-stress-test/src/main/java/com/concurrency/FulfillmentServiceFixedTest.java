package com.concurrency;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@JCStressTest
@Outcome(id = "FC-7", expect = Expect.ACCEPTABLE, desc = "old config")
@Outcome(id = "FC-5", expect = Expect.ACCEPTABLE, desc = "new config")
@Outcome(id = "UNROUTABLE", expect = Expect.FORBIDDEN, desc = "torn - order rejected")
@Outcome(id = "NO_CUTOFF", expect = Expect.FORBIDDEN, desc = "torn - null cutoff")
@State
public class FulfillmentServiceFixedTest {

    public record WarehouseRoutes(Set<String> active,
                               Map<String, String> routing,
                               Map<String, LocalTime> cutoffs) {
    public WarehouseRoutes {
        active = Set.copyOf(active);
        routing = Map.copyOf(routing);
        cutoffs = Map.copyOf(cutoffs);
    }

    public static WarehouseRoutes empty() {
        return new WarehouseRoutes(Set.of(), Map.of(), Map.of());
    }
}

    static final WarehouseRoutes OLD_ROUTES = new WarehouseRoutes(
            Set.of("FC-7"),
            Map.of("ABC", "FC-7"),
            Map.of("FC-7", LocalTime.of(17, 0))
    );

    static final WarehouseRoutes NEW_ROUTES = new WarehouseRoutes(
            Set.of("FC-5"),
            Map.of("ABC", "FC-5"),
            Map.of("FC-5", LocalTime.of(15, 0))
    );

    volatile WarehouseRoutes current = OLD_ROUTES;

    @Actor
    public void refresh() {
        current = NEW_ROUTES;
    }

    @Actor
    public void route(L_Result r) {
        WarehouseRoutes t = current;
        String warehouse = t.routing().get("ABC");
        if (warehouse == null || !t.active().contains(warehouse)) {
            r.r1 = "UNROUTABLE";
            return;
        }
        if (t.cutoffs().get(warehouse) == null) {
            r.r1 = "NO_CUTOFF";
            return;
        }
        r.r1 = warehouse;
    }
}