package com.vortex.domain.cell;

import com.vortex.domain.Point;
import lombok.Data;

@Data
public class ResolvedCell {

    public enum Resolution {
        OPENED, INFECTED
    }

    private Resolution resolution;
    private Point point;
}
