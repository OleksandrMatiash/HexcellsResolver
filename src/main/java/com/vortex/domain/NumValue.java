package com.vortex.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NumValue {

    public enum Type {
        SIMPLE_VALUE, IN_A_ROW, NOT_IN_A_ROW
    }

    private Type type;
    private int value;
}
