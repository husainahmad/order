package com.harmoni.pos.order.utils;

import java.util.ArrayList;
import java.util.Arrays;

 public final class PosObjectUtils {

     private PosObjectUtils() {
     }

     public static Object[] appendValue(Object[] obj, String value) {
        ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(value);
        return temp.toArray();
     }
}
