/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danius.fireeditor.compression;

import java.util.HashMap;

/**
 * @author Edgar
 */
public class Uint8Array {
    private HashMap<Long, Number> arregloMap;

    public Uint8Array(long length) {
        arregloMap = new HashMap<>();
        for (long i = 0; i < length; i++) {
            arregloMap.put(i, 0);
        }
    }

    public Uint8Array(byte[] s) {
        arregloMap = new HashMap<>();
        for (long i = 0; i < s.length; i++) {
            int part = s[(int) i];
            if (part < 0) {
                part = part & 0xff;
            }
            arregloMap.put(i, part);
        }
    }

    public void set(long pos, long val) {
        if (val < 0) {
            val = val & 0xff;
        }
        arregloMap.put(pos, (byte) val);
    }

    public long get(long i) {
        return arregloMap.get(i).longValue();
    }

    public long length() {
        return arregloMap.size();
    }
}