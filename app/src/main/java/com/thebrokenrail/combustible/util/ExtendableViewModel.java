package com.thebrokenrail.combustible.util;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple View Model that allows attaching extra data.
 */
public class ExtendableViewModel extends ViewModel {
    private final Map<Class<?>, Object> data = new HashMap<>();

    /**
     * Get (or create if needed) an object.
     * @param t The type of object
     * @return The object
     * @param <T> The object's class
     */
    @SuppressWarnings({"unchecked"})
    public <T> T get(Class<T> t) {
        if (data.containsKey(t)) {
            return (T) data.get(t);
        } else {
            try {
                T obj = t.newInstance();
                data.put(t, obj);
                return obj;
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
