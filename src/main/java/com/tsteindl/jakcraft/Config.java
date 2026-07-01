package com.tsteindl.jakcraft;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

// Configuration for the Müllagerking final boss.
//
// The lists below hold the German "voice lines" the boss shows in chat.
// They are fully user-editable at runtime: edit config/jakcraft-common.toml
// (run/config/ in dev, the instance's config/ folder when playing) and add or
// change lines - one entry per line, no rebuild needed. When a moment triggers,
// one line is picked at random from the matching list.
public class Config {

  public static final ForgeConfigSpec SPEC;

  // Played once, when the king first notices a player and the fight begins.
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FIGHT_START_LINES;
  // Played whenever the king starts a regular attack.
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ATTACK_LINES;
  // Played when the king casts the Fourier-Welle attack.
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FOURIER_LINES;
  // Played when the king summons his "Passenbrunner" helpers.
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PASSENBRUNNER_LINES;
  // Played when the king summons his "Lechner" helpers.
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LECHNER_LINES;
  // Played when the king dies.
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEATH_LINES;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    builder.comment("Müllagerking final boss - Paul Müller, Jakobs Doktorvater (harmonische Analysis).",
            "Alle Zeilen erscheinen im Chat. Eigene Zeilen einfach hinzufügen.")
        .push("muellagerking");

    FIGHT_START_LINES = builder
        .comment("Zeilen zu Kampfbeginn (eine wird zufällig gewählt).")
        .defineList("lines_fight_start",
            List.of(
                "Jakob! Deine Dissertation endet hier.",
                "Zeit für die Verteidigung, Herr Doktorand.",
                "Harmonische Analysis kennt kein Erbarmen."),
            obj -> obj instanceof String);

    ATTACK_LINES = builder
        .comment("Zeilen bei einem normalen Angriff (eine wird zufällig gewählt).")
        .defineList("lines_attack",
            List.of(
                "Diese Reihe konvergiert - gegen deinen Untergang!",
                "Ein singuläres Integral, direkt ins Herz!",
                "Zerlege dich in deine Oberschwingungen!",
                "Kein Beweis rettet dich jetzt."),
            obj -> obj instanceof String);

    FOURIER_LINES = builder
        .comment("Zeilen bei der Fourier-Welle (eine wird zufällig gewählt).")
        .defineList("lines_fourier",
            List.of(
                "Fourierwelle!",
                "Spüre die Fourier-Transformation!",
                "Diese Welle konvergiert gegen deinen Untergang!"),
            obj -> obj instanceof String);

    PASSENBRUNNER_LINES = builder
        .comment("Zeilen beim Beschwören der Passenbrunner (eine wird zufällig gewählt).")
        .defineList("lines_passenbrunner",
            List.of(
                "Passenbrunner, an die Arbeit!",
                "Herr Passenbrunner, übernehmen Sie!",
                "Die Passenbrunner werden dich lehren!"),
            obj -> obj instanceof String);

    LECHNER_LINES = builder
        .comment("Zeilen beim Beschwören der Lechner (eine wird zufällig gewählt).")
        .defineList("lines_lechner",
            List.of(
                "Lechner, zeig ihm die Grenzen!",
                "Lechner, an die Tafel!",
                "Auf sie, meine Lechner!"),
            obj -> obj instanceof String);

    DEATH_LINES = builder
        .comment("Zeilen beim Tod des Königs (eine wird zufällig gewählt).")
        .defineList("lines_death",
            List.of(
                "Beeindruckend... vielleicht doch... promotionswürdig..."),
            obj -> obj instanceof String);

    builder.pop();
    SPEC = builder.build();
  }
}
