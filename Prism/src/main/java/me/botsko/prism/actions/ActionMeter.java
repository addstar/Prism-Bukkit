package me.botsko.prism.actions;

import au.com.addstar.dripreporter.DripMeter;
import com.google.common.collect.ImmutableMap;
import me.botsko.prism.ApiHandler;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.actions.Handler;
import java.util.HashMap;
import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 25/02/2020.
 */
public class ActionMeter {

    private static final Map<String, DripMeter> meter = new HashMap<>();
    private static boolean monitoring = false;
    private static final Map<String, Integer> metricMeter = new HashMap<>();

    static {
        if (Prism.getInstance().monitoring) {
            addClass(GenericAction.class);
            addClass(EntityAction.class);
            addClass(BlockAction.class);
            addClass(BlockChangeAction.class);
            addClass(ItemStackAction.class);
            addClass(BlockShiftAction.class);
            addClass(EntityTravelAction.class);
            addClass(GrowAction.class);
            addClass(HangingItemAction.class);
            addClass(PlayerAction.class);
            addClass(PlayerDeathAction.class);
            addClass(PrismProcessAction.class);
            addClass(PrismRollbackAction.class);
            addClass(SignAction.class);
            addClass(VehicleAction.class);
            meter.put("UnknownHandler", getMeter(Handler.class));
            PrismLogHandler.log("Action Meter metrics enabled. " + meter.size() + " metrics registered");
            monitoring = true;
        }
    }

    /**
     * Returns a map of Metrics for the actions.
     *
     * @return Map
     */
    public static Map<String, Integer> getMetricMeter() {
        Map<String, Integer> out = new HashMap<>(metricMeter);
        metricMeter.clear();
        return ImmutableMap.copyOf(out);
    }

    static void addClass(Class<? extends Handler> clazz) {
        meter.put(clazz.getSimpleName(), getMeter(clazz));
        metricMeter.put(clazz.getSimpleName(), 0);
    }

    @SuppressWarnings("rawtypes")
    private static DripMeter getMeter(Class clazz) {
        if (meter.containsKey(clazz.getSimpleName())) {
            return meter.get(clazz.getSimpleName());
        } else {
            return ApiHandler.monitor.addMeter(clazz);
        }
    }

    /**
     * Setup the meter to record.
     */
    public static void setupActionMeter() {
        monitoring = Prism.getInstance().monitoring;
    }

    /**
     * Mark a event.
     *
     * @param clazz Class
     */
    public static void mark(Class<? extends Handler> clazz) {
        if (monitoring) {
            DripMeter m = meter.get(clazz.getSimpleName());
            if (m == null) {
                m = meter.get("UnknownHandler");
            }
            if (m != null) {
                m.mark();
            }
        }
        Integer value = metricMeter.getOrDefault(clazz.getSimpleName(), 0);
        metricMeter.put(clazz.getSimpleName(), (value + 1));
    }

    public static void flushMetric() {
        metricMeter.clear();
    }
}
