package com.lyk.log;

public interface HiLogFormatter<T> {

    String format(T data);
}