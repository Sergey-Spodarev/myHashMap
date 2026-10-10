package org.example;

public class TreeNode<Key, Value> extends Node<Key, Value> {

    TreeNode<Key, Value> parent;
    TreeNode<Key, Value> left;
    TreeNode<Key, Value> right;
    TreeNode<Key, Value> prev;
    boolean red;

    public TreeNode(int hash, Key key, Value value, Node<Key, Value> next) {
        super(hash, key, value, next);
    }

    final TreeNode<Key, Value> root() {
        for (TreeNode<Key, Value> r = this, p;;) {
            if ((p = r.parent) == null) return r;
            r = p;
        }
    }

    final TreeNode<Key, Value> getTreeNode(int h, Object k) {
        return ((parent != null) ? root() : this).find(h, k, null);
    }

    final TreeNode<Key, Value> find(int h, Object k, Class<?> kc) {
        TreeNode<Key, Value> p = this;
        do {
            int ph, dir;
            Key pk;
            TreeNode<Key, Value> pl = p.left, pr = p.right, q;
            if ((ph = p.hash) > h) p = pl;
            else if (ph < h) p = pr;
            else if ((pk = p.key) == k || (k != null && k.equals(pk))) return p;
            else if (pl == null) p = pr;
            else if (pr == null) p = pl;
            else if ((kc != null || (kc = comparableClassFor(k)) != null) &&
                    (dir = compareComparables(kc, k, pk)) != 0)
                p = (dir < 0) ? pl : pr;
            else if ((q = pr.find(h, k, kc)) != null) return q;
            else p = pl;
        } while (p != null);
        return null;
    }

    final TreeNode<Key, Value> putTreeVal(Node<Key, Value>[] tab, int h, Key k, Value v) {
        Class<?> kc = null;
        boolean searched = false;
        TreeNode<Key, Value> root = (parent != null) ? root() : this;

        for (TreeNode<Key, Value> p = root;;) {
            int dir, ph;
            Key pk;

            if ((ph = p.hash) > h) dir = -1;
            else if (ph < h) dir = 1;
            else if ((pk = p.key) == k || (k != null && k.equals(pk))) return p;
            else if ((kc == null && (kc = comparableClassFor(k)) == null) ||
                    (dir = compareComparables(kc, k, pk)) == 0) {
                if (!searched) {
                    TreeNode<Key, Value> q, ch;
                    searched = true;
                    if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null && (q = ch.find(h, k, kc)) != null))
                        return q;
                }
                dir = tieBreakOrder(k, pk);
            }

            TreeNode<Key, Value> xp = p;
            if ((p = (dir <= 0) ? p.left : p.right) == null) {
                Node<Key, Value> xpn = xp.next;
                TreeNode<Key, Value> x = new TreeNode<>(h, k, v, xpn);
                if (dir <= 0) xp.left = x;
                else xp.right = x;
                xp.next = x;
                x.parent = x.prev = xp;
                if (xpn != null) ((TreeNode<Key, Value>) xpn).prev = x;
                moveRootToFront(tab, balanceInsertion(root, x));
                return null;
            }
        }
    }

    final void treeify(Node<Key, Value>[] tab) {
        TreeNode<Key, Value> root = null;

        for (TreeNode<Key, Value> x = this, next; x != null; x = next) {
            next = (TreeNode<Key, Value>) x.next;
            x.left = x.right = null;

            if (root == null) {
                x.parent = null;
                x.red = false;
                root = x;
            } else {
                Key k = x.key;
                int h = x.hash;
                Class<?> kc = null;

                for (TreeNode<Key, Value> p = root;;) {
                    int dir, ph;
                    Key pk = p.key;

                    if ((ph = p.hash) > h) dir = -1;
                    else if (ph < h) dir = 1;
                    else if ((kc == null && (kc = comparableClassFor(k)) == null) ||
                            (dir = compareComparables(kc, k, pk)) == 0) {
                        dir = tieBreakOrder(k, pk);
                    }

                    TreeNode<Key, Value> xp = p;

                    if ((p = (dir <= 0) ? p.left : p.right) == null) {
                        x.parent = xp;
                        if (dir <= 0) xp.left = x;
                        else xp.right = x;
                        root = balanceInsertion(root, x);
                        break;
                    }
                }
            }
        }
        moveRootToFront(tab, root);
    }

    final void removeTreeNode(Node<Key, Value>[] tab, boolean movable) {
        int n;
        if (tab == null || (n = tab.length) == 0) return;
        int index = (n - 1) & hash;

        TreeNode<Key, Value> first = (TreeNode<Key, Value>) tab[index];
        TreeNode<Key, Value> root = first;
        TreeNode<Key, Value> rl;

        TreeNode<Key, Value> succ = (TreeNode<Key, Value>) next;
        TreeNode<Key, Value> pred = prev;

        if (pred == null) tab[index] = first = succ;
        else pred.next = succ;
        if (succ != null) succ.prev = pred;
        if (first == null) return;

        if (root.parent != null) root = root.root();
        if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
            tab[index] = untreeify(first);
            return;
        }

        TreeNode<Key, Value> p = this, pl = left, pr = right, replacement;

        if (pl != null && pr != null) {
            TreeNode<Key, Value> s = pr, sl;
            while ((sl = s.left) != null) s = sl;

            boolean c = s.red; s.red = p.red; p.red = c;
            TreeNode<Key, Value> sr = s.right;
            TreeNode<Key, Value> pp = p.parent;

            if (s == pr) {
                p.parent = s;
                s.right = p;
            } else {
                TreeNode<Key, Value> sp = s.parent;
                if ((p.parent = sp) != null) {
                    if (s == sp.left) sp.left = p;
                    else sp.right = p;
                }
                if ((s.right = pr) != null) pr.parent = s;
            }
            p.left = null;
            if ((p.right = sr) != null) sr.parent = p;
            if ((s.left = pl) != null) pl.parent = s;
            if ((s.parent = pp) == null) root = s;
            else if (p == pp.left) pp.left = s;
            else pp.right = s;

            replacement = (sr != null) ? sr : p;
        } else if (pl != null) {
            replacement = pl;
        } else if (pr != null) {
            replacement = pr;
        } else {
            replacement = p;
        }

        if (replacement != p) {
            TreeNode<Key, Value> pp = replacement.parent = p.parent;
            if (pp == null) root = replacement;
            else if (p == pp.left) pp.left = replacement;
            else pp.right = replacement;
            p.left = p.right = p.parent = null;
        }

        TreeNode<Key, Value> r = p.red ? root : balanceDeletion(root, replacement);

        if (replacement == p) {
            TreeNode<Key, Value> pp = p.parent;
            p.parent = null;
            if (pp != null) {
                if (p == pp.left) pp.left = null;
                else if (p == pp.right) pp.right = null;
            }
        }
        if (movable) moveRootToFront(tab, r);
    }

    final Node<Key, Value> untreeify(Node<Key, Value> head) {
        Node<Key, Value> hd = null, tl = null;
        for (Node<Key, Value> q = head; q != null; q = q.next) {
            Node<Key, Value> p = new Node<>(q.hash, q.key, q.value, null);
            if (tl == null) hd = p;
            else tl.next = p;
            tl = p;
        }
        return hd;
    }

    static <Key, Value> TreeNode<Key, Value> rotateLeft(TreeNode<Key, Value> root, TreeNode<Key, Value> p) {
        TreeNode<Key, Value> r, pp, rl;
        if (p != null && (r = p.right) != null) {
            if ((rl = p.right = r.left) != null) rl.parent = p;
            if ((pp = r.parent = p.parent) == null) (root = r).red = false;
            else if (pp.left == p) pp.left = r;
            else pp.right = r;
            r.left = p;
            p.parent = r;
        }
        return root;
    }

    static <Key, Value> TreeNode<Key, Value> rotateRight(TreeNode<Key, Value> root, TreeNode<Key, Value> p) {
        TreeNode<Key, Value> l, pp, lr;
        if (p != null && (l = p.left) != null) {
            if ((lr = p.left = l.right) != null) lr.parent = p;
            if ((pp = l.parent = p.parent) == null) (root = l).red = false;
            else if (pp.right == p) pp.right = l;
            else pp.left = l;
            l.right = p;
            p.parent = l;
        }
        return root;
    }

    static <Key, Value> TreeNode<Key, Value> balanceInsertion(TreeNode<Key, Value> root, TreeNode<Key, Value> x) {
        x.red = true;
        for (TreeNode<Key, Value> xp, xpp, xppl, xppr;;) {
            if ((xp = x.parent) == null) { x.red = false; return x; }
            else if (!xp.red || (xpp = xp.parent) == null) return root;

            if (xp == (xppl = xpp.left)) {
                if ((xppr = xpp.right) != null && xppr.red) {
                    xppr.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                } else {
                    if (x == xp.right) {
                        root = rotateLeft(root, x = xp);
                        xpp = (xp = x.parent) == null ? null : xp.parent;
                    }
                    if (xp != null) {
                        xp.red = false;
                        if (xpp != null) {
                            xpp.red = true;
                            root = rotateRight(root, xpp);
                        }
                    }
                }
            } else {
                if (xppl != null && xppl.red) {
                    xppl.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                } else {
                    if (x == xp.left) {
                        root = rotateRight(root, x = xp);
                        xpp = (xp = x.parent) == null ? null : xp.parent;
                    }
                    if (xp != null) {
                        xp.red = false;
                        if (xpp != null) {
                            xpp.red = true;
                            root = rotateLeft(root, xpp);
                        }
                    }
                }
            }
        }
    }

    static <Key, Value> TreeNode<Key, Value> balanceDeletion(TreeNode<Key, Value> root, TreeNode<Key, Value> x) {
        for (TreeNode<Key, Value> xp, xpl, xpr;;) {
            if (x == null || x == root) return root;
            else if ((xp = x.parent) == null) { x.red = false; return x; }
            else if (x.red) { x.red = false; return root; }

            else if ((xpl = xp.left) == x) {
                if ((xpr = xp.right) != null && xpr.red) {
                    xpr.red = false;
                    xp.red = true;
                    root = rotateLeft(root, xp);
                    xpr = (xp = x.parent) == null ? null : xp.right;
                }
                if (xpr == null) {
                    x = xp;
                } else {
                    TreeNode<Key, Value> sl = xpr.left, sr = xpr.right;
                    if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                        xpr.red = true;
                        x = xp;
                    } else {
                        if (sr == null || !sr.red) {
                            if (sl != null) sl.red = false;
                            xpr.red = true;
                            root = rotateRight(root, xpr);
                            xpr = (xp = x.parent) == null ? null : xp.right;
                        }
                        if (xpr != null) {
                            xpr.red = (xp == null) ? false : xp.red;
                            if ((sr = xpr.right) != null) sr.red = false;
                        }
                        if (xp != null) {
                            xp.red = false;
                            root = rotateLeft(root, xp);
                        }
                        x = root;
                    }
                }
            } else {
                if (xpl != null && xpl.red) {
                    xpl.red = false;
                    xp.red = true;
                    root = rotateRight(root, xp);
                    xpl = (xp = x.parent) == null ? null : xp.left;
                }
                if (xpl == null) {
                    x = xp;
                } else {
                    TreeNode<Key, Value> sl = xpl.left, sr = xpl.right;
                    if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                        xpl.red = true;
                        x = xp;
                    } else {
                        if (sl == null || !sl.red) {
                            if (sr != null) sr.red = false;
                            xpl.red = true;
                            root = rotateLeft(root, xpl);
                            xpl = (xp = x.parent) == null ? null : xp.left;
                        }
                        if (xpl != null) {
                            xpl.red = (xp == null) ? false : xp.red;
                            if ((sl = xpl.left) != null) sl.red = false;
                        }
                        if (xp != null) {
                            xp.red = false;
                            root = rotateRight(root, xp);
                        }
                        x = root;
                    }
                }
            }
        }
    }

    static <Key, Value> void moveRootToFront(Node<Key, Value>[] tab, TreeNode<Key, Value> root) {
        int n;
        if (root != null && tab != null && (n = tab.length) > 0) {
            int index = (n - 1) & root.hash;
            TreeNode<Key, Value> first = (TreeNode<Key, Value>) tab[index];
            if (root != first) {
                Node<Key, Value> rn;
                tab[index] = root;
                TreeNode<Key, Value> rp = root.prev;
                if ((rn = root.next) != null) ((TreeNode<Key, Value>) rn).prev = rp;
                if (rp != null) rp.next = rn;
                if (first != null) first.prev = root;
                root.next = first;
                root.prev = null;
            }
        }
    }

    static Class<?> comparableClassFor(Object x) {
        return (x instanceof Comparable) ? x.getClass() : null;
    }

    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc) ? 0 : ((Comparable) k).compareTo(x);
    }

    static int tieBreakOrder(Object a, Object b) {
        int d;
        if (a == null || b == null ||
                (d = System.identityHashCode(a) - System.identityHashCode(b)) == 0) {
            d = (a == null ? 0 : 1) - (b == null ? 0 : 1);
        }
        return d;
    }
}
