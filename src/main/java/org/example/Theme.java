package org.example;

import java.awt.*;

/**
 * Central design token class for AppointEase UI.
 * All colors, fonts, and spacing constants live here.
 *
 * @author AppointEase
 * @version 1.0
 */
public final class Theme {

    private Theme() {}

    
    public static final Color INK        = new Color(0x0F, 0x0E, 0x0B);
    public static final Color PAPER      = new Color(0xF5, 0xF0, 0xE8);
    public static final Color CARD       = new Color(0xFA, 0xF7, 0xF2);
    public static final Color CREAM      = new Color(0xED, 0xE7, 0xD9);
    public static final Color BORDER     = new Color(0xD4, 0xCB, 0xBF);
    public static final Color MUTED      = new Color(0x8A, 0x80, 0x70);
    public static final Color SIDEBAR_BG = new Color(0x14, 0x11, 0x0D);

    
    public static final Color ACCENT     = new Color(0xC8, 0x4B, 0x2F);
    public static final Color ACCENT2    = new Color(0x2F, 0x6F, 0xC8);
    public static final Color SUCCESS    = new Color(0x2A, 0x7A, 0x4B);
    public static final Color WARNING    = new Color(0xC8, 0x7D, 0x2F);
    public static final Color WHITE      = Color.WHITE;

    
    public static final Color TAG_CONFIRMED_BG = new Color(0xD4, 0xED, 0xDF);
    public static final Color TAG_PENDING_BG   = new Color(0xFD, 0xEB, 0xD2);
    public static final Color TAG_URGENT_BG    = new Color(0xFA, 0xD5, 0xD0);
    public static final Color TAG_VIRTUAL_BG   = new Color(0xD2, 0xE1, 0xFA);
    public static final Color TAG_GROUP_BG     = new Color(0xEC, 0xE3, 0xFA);
    public static final Color TAG_GROUP_FG     = new Color(0x6B, 0x3F, 0xC8);

    
    
    public static final Font FONT_TITLE  = new Font("Serif",     Font.BOLD,  26);
    public static final Font FONT_HEAD   = new Font("Serif",     Font.BOLD,  18);

    
    public static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);

    
    
    public static final Font FONT_LABEL  = new Font("SansSerif", Font.BOLD,  11);

    
    public static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);

    
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD,  12);

    
    public static final int PAD_LG = 24;
    public static final int PAD_MD = 16;
    public static final int PAD_SM = 8;
    public static final int RADIUS = 4;
}
