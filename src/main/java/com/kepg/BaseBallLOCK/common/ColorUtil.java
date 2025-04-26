package com.kepg.BaseBallLOCK.common;

import org.springframework.stereotype.Component;

@Component("colorUtil")
public class ColorUtil {
    public String toRgba(String hexColor, double alpha) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return "255, 255, 255, 0.3"; // fallback: 흰색
        }

        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        return String.format("%d, %d, %d, %.2f", r, g, b, alpha);
    }
}