package org.example;

public final class myHashMap<Key, Value> implements myMap<Key, Value> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int TREEIFY_THRESHOLD = 8;
    private static final int MIN_TREEIFY_CAPACITY = 64;

    private Node<Key, Value>[] table;
    private int size;
    private int threshold;

    @SuppressWarnings("unchecked")
    public myHashMap() {
        table = (Node<Key, Value>[]) new Node[DEFAULT_CAPACITY];
        threshold = (int) (DEFAULT_CAPACITY * DEFAULT_LOAD_FACTOR);
    }

    private int hash(Key key) {
        int h = (key == null) ? 0 : key.hashCode();
        return h ^ (h >>> 16);
    }

    private int indexFor(int hash, int length) {
        return (length - 1) & hash;
    }


    @Override
    public Value put(Key key, Value value) {
        int h = hash(key);
        int i = indexFor(h, table.length);

        if (table[i] == null) {
            table[i] = new Node<>(h, key, value, null);
            size++;
            if (size > threshold) resize();
            return null;
        }

        if (table[i] instanceof TreeNode) {
            TreeNode<Key, Value> t = ((TreeNode<Key, Value>) table[i]).putTreeVal(table, h, key, value);
            if (t != null) {
                Value old = t.value;
                t.value = value;
                return old;
            }
            size++;
            if (size > threshold) resize();
            return null;
        }

        Node<Key, Value> e = table[i];
        int binCount = 0;
        while (true) {
            if (e.hash == h && (e.key == key || (key != null && key.equals(e.key)))) {
                Value old = e.value;
                e.value = value;
                return old;
            }
            if (e.next == null) {
                e.next = new Node<>(h, key, value, null);
                size++;
                if (binCount >= TREEIFY_THRESHOLD - 1) treeifyBin(table, h);
                else if (size > threshold) resize();
                return null;
            }
            e = e.next;
            binCount++;
        }
    }

    @Override
    public Value get(Key key) {
        int h = hash(key);
        int i = indexFor(h, table.length);

        Node<Key, Value> e = table[i];
        if (e == null) return null;

        if (e instanceof TreeNode) {
            TreeNode<Key, Value> t = ((TreeNode<Key, Value>) e).getTreeNode(h, key);
            return (t == null) ? null : t.value;
        }

        for (; e != null; e = e.next) {
            if (e.hash == h && (e.key == key || (key != null && key.equals(e.key)))) {
                return e.value;
            }
        }
        return null;
    }

    @Override
    public Value remove(Key key) {
        int h = hash(key);
        int i = indexFor(h, table.length);

        Node<Key, Value> e = table[i];
        if (e == null) return null;

        if (e instanceof TreeNode) {
            TreeNode<Key, Value> t = ((TreeNode<Key, Value>) e).getTreeNode(h, key);
            if (t == null) return null;
            Value old = t.value;
            t.removeTreeNode(table, true);
            size--;
            return old;
        }

        Node<Key, Value> prev = null;
        while (e != null) {
            if (e.hash == h && (e.key == key || (key != null && key.equals(e.key)))) {
                if (prev == null) table[i] = e.next;
                else prev.next = e.next;
                size--;
                return e.value;
            }
            prev = e;
            e = e.next;
        }
        return null;
    }

    public int size() {
        return size;
    }

    private void treeifyBin(Node<Key, Value>[] tab, int hash) {
        int n, index;
        Node<Key, Value> e;

        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) {
            resize();
        } else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<Key, Value> head = null, tail = null;

            do {
                TreeNode<Key, Value> p = new TreeNode<>(e.hash, e.key, e.value, null);
                if (tail == null) head = p;
                else {
                    p.prev = tail;
                    tail.next = p;
                }
                tail = p;
                e = e.next;
            } while (e != null);

            tab[index] = head;
            head.treeify(tab);
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<Key, Value>[] oldTable = table;
        int oldCap = oldTable.length;
        int newCap = oldCap << 1;

        Node<Key, Value>[] newTable = (Node<Key, Value>[]) new Node[newCap];
        table = newTable;
        threshold = (int) (newCap * DEFAULT_LOAD_FACTOR);

        for (int j = 0; j < oldCap; j++) {
            Node<Key, Value> e = oldTable[j];
            if (e == null) continue;
            oldTable[j] = null;

            if (e.next == null) {
                newTable[e.hash & (newCap - 1)] = e;
            } else {
                Node<Key, Value> loHead = null, loTail = null;
                Node<Key, Value> hiHead = null, hiTail = null;
                Node<Key, Value> next;

                do {
                    next = e.next;
                    if ((e.hash & oldCap) == 0) {
                        if (loTail == null) loHead = e;
                        else loTail.next = e;
                        loTail = e;
                    } else {
                        if (hiTail == null) hiHead = e;
                        else hiTail.next = e;
                        hiTail = e;
                    }
                    e = next;
                } while (e != null);

                if (loTail != null) {
                    loTail.next = null;
                    newTable[j] = loHead;
                }
                if (hiTail != null) {
                    hiTail.next = null;
                    newTable[j + oldCap] = hiHead;
                }
            }
        }
    }
}
