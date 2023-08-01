package custom;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class ReflectUtils {

    @SuppressWarnings({"unchecked"})
    public static <T, K> void invokeField(Object o,
                                          Map<String, K> valueMap,
                                          BiConsumer<T, K> doSth) throws IllegalAccessException {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field each : fields) {
            each.setAccessible(true);
            String key = each.getName();
            K value = valueMap.get(key);
            if (value != null) {
                doSth.accept((T) each.get(o), value);
            }
        }
    }

}
