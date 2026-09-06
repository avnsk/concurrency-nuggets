package com.concurrency;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@JCStressTest
@Outcome(id = "FC-7", expect = Expect.ACCEPTABLE, desc = "old config")
@Outcome(id = "FC-5", expect = Expect.ACCEPTABLE, desc = "new config")
@Outcome(id = "UNROUTABLE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "torn - order rejected")
@Outcome(id = "NO_CUTOFF", expect = Expect.ACCEPTABLE_INTERESTING, desc = "torn - null cutoff")
@State
public class FulfillmentServiceIncorrectTest {

    static final Set<String> OLD_ACTIVE = Set.of("FC-7");
    static final Set<String> NEW_ACTIVE = Set.of("FC-5");
    static final Map<String, String> OLD_ROUTING = Map.of("ABC", "FC-7");
    static final Map<String, String> NEW_ROUTING = Map.of("ABC", "FC-5");
    static final Map<String, LocalTime> OLD_CUTOFF = Map.of("FC-7", LocalTime.of(17, 0));
    static final Map<String, LocalTime> NEW_CUTOFF = Map.of("FC-5", LocalTime.of(15, 0));

    volatile Set<String> active = OLD_ACTIVE;
    volatile Map<String, String> routing = OLD_ROUTING;
    volatile Map<String, LocalTime> cutoffs = OLD_CUTOFF;

    @Actor
    public void refresh() {
        active = NEW_ACTIVE;
        routing = NEW_ROUTING;
        cutoffs = NEW_CUTOFF;
    }

    @Actor
    public void route(L_Result r) {
        String warehouse = routing.get("ABC");
        if (warehouse == null || !active.contains(warehouse)) {
            r.r1 = "UNROUTABLE";
            return;
        }
        if (cutoffs.get(warehouse) == null) {
            r.r1 = "NO_CUTOFF";
            return;
        }
        r.r1 = warehouse;
    }
}