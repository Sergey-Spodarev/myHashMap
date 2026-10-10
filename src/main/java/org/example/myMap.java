package org.example;

public interface myMap<Key, Value> {
    Value get(Key key);
    Value put(Key key, Value value);
    Value remove(Key key);
}
