package org.example;

public class Node<Key, Value> {
    int hash;
    Key key;
    Value value;
    Node<Key, Value> next;

    public Node(int hash, Key key, Value value, Node<Key, Value> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}
