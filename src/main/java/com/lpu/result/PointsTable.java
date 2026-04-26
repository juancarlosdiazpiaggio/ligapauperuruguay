package com.lpu.result;

import org.springframework.stereotype.Component;

@Component
public class PointsTable {

    public int pointsFor(int position) {
        if (position == 1)       return 20;
        if (position == 2)       return 15;
        if (position <= 4)       return 10;
        if (position <= 8)       return 6;
        if (position <= 16)      return 3;
        return 1;
    }
}
