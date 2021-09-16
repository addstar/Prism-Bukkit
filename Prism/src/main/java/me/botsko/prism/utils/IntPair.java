package me.botsko.prism.utils;

public class IntPair {
    public int first;
    public int second;

    public IntPair() {
    }

    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public IntPair(long both) {
        first = (int) (both >> 32);
        second = (int) both;
    }

    public long getBoth() {
        return (((long) first) << 32) | (second & 0xffffffffL);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getBoth());
    }
}
