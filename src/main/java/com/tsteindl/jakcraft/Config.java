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
  // Multiplier applied to the king's base max health (1.0 = unchanged).
  public static final ForgeConfigSpec.DoubleValue MUELLAGER_HEALTH_MULTIPLIER;

  // If true, the Helfer and Papst NPCs are kept present at their coordinates (for testing).
  public static final ForgeConfigSpec.BooleanValue STORY_AUTO_SPAWN;

  // Name shown for the player-character dialogue lines (emulates that player chatting).
  public static final ForgeConfigSpec.ConfigValue<String> PROTAGONIST_NAME;

  // Helfer on the plaza in front of the basilica.
  public static final ForgeConfigSpec.ConfigValue<Integer> HELPER_SPAWN_X;
  public static final ForgeConfigSpec.ConfigValue<Integer> HELPER_SPAWN_Y;
  public static final ForgeConfigSpec.ConfigValue<Integer> HELPER_SPAWN_Z;
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HELPER_DIALOGUE;

  // Papst at the altar inside the basilica; transforms into the Müllagerking.
  public static final ForgeConfigSpec.ConfigValue<Integer> POPE_SPAWN_X;
  public static final ForgeConfigSpec.ConfigValue<Integer> POPE_SPAWN_Y;
  public static final ForgeConfigSpec.ConfigValue<Integer> POPE_SPAWN_Z;
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> POPE_DIALOGUE;

  // Victory: after the king dies and the player picks up the PhD scroll, they are teleported.
  public static final ForgeConfigSpec.ConfigValue<Integer> VICTORY_TELEPORT_DELAY_SECONDS;
  public static final ForgeConfigSpec.ConfigValue<Integer> VICTORY_FALLBACK_SECONDS;
  public static final ForgeConfigSpec.ConfigValue<Integer> VICTORY_PLATFORM_X;
  public static final ForgeConfigSpec.ConfigValue<Integer> VICTORY_PLATFORM_Y;
  public static final ForgeConfigSpec.ConfigValue<Integer> VICTORY_PLATFORM_Z;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    builder.comment("Müllagerking final boss - Paul Müller, Jakobs Doktorvater (harmonische Analysis).",
            "Alle Zeilen erscheinen im Chat. Eigene Zeilen einfach hinzufügen.")
        .push("muellagerking");

    MUELLAGER_HEALTH_MULTIPLIER = builder
        .comment("Multiplikator für Müllers maximale Lebenspunkte (1.0 = unverändert, höher = mehr HP).")
        .defineInRange("health_multiplier", 1.3, 1.0, 1000.0);

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
                "Herr Passenbrunner wird dich lehren!"),
            obj -> obj instanceof String);

    LECHNER_LINES = builder
        .comment("Zeilen beim Beschwören der Lechner (eine wird zufällig gewählt).")
        .defineList("lines_lechner",
            List.of(
                "Lechner, zeig ihm die Grenzen!",
                "Lechner, an die Tafel!",
                "Auf ihn, Herr Lechner!"),
            obj -> obj instanceof String);

    DEATH_LINES = builder
        .comment("Zeilen beim Tod des Königs (eine wird zufällig gewählt).")
        .defineList("lines_death",
            List.of(
                "Beeindruckend... vielleicht doch... promotionswürdig..."),
            obj -> obj instanceof String);

    builder.pop();

    builder.comment("Papst-Handlung: Helfer auf dem Vorplatz, Papst am Altar, Verwandlung und Sieg-Teleport.")
        .push("story");

    STORY_AUTO_SPAWN = builder
        .comment("Wenn true, werden Helfer und Papst automatisch an ihren Koordinaten platziert, sobald ein",
                "Spieler in der Nähe ist. Standard: false - platziere die NPCs stattdessen von Hand mit ihren",
                "Spawn-Eiern (Helfer- bzw. Papst-Spawn-Ei). Auf true setzen, wenn du automatisches Platzieren willst.")
        .define("auto_spawn", false);

    PROTAGONIST_NAME = builder
        .comment("Name für die Spieler-Zeilen im Dialog (Sprecher \"Spieler\", \"Jakob\" oder \"Player\").",
                "Die Zeile wird als ganz normale Chat-Nachricht dieses Spielers angezeigt: \"<Name> Text\".",
                "So wirkt es, als würde dieser Spieler die Zeile selbst in den Chat schreiben.")
        .define("protagonist_name", "TheRealMikeJohn");

    HELPER_SPAWN_X = builder.comment("X-Koordinate des Helfers (Vorplatz des Petersdoms).").define("helper_spawn_x", 129);
    HELPER_SPAWN_Y = builder.comment("Y-Koordinate (Höhe) des Helfers - je nach Welt anpassen.").define("helper_spawn_y", 69);
    HELPER_SPAWN_Z = builder.comment("Z-Koordinate des Helfers.").define("helper_spawn_z", -152);

    HELPER_DIALOGUE = builder
        .comment("Dialog mit dem Helfer auf dem Vorplatz. Format je Zeile: \"Sprecher|Text\".",
                "Sprecher \"Helfer\" = grau (fett), \"Spieler\"/\"Jakob\"/\"Player\" = als Chat-Nachricht von",
                "protagonist_name (\"<Name> Text\"), alles andere = Papst (gold, fett).",
                "Der Helfer verwandelt sich NICHT - er schickt dich hinein zum Papst am Altar.")
        .defineList("helper_dialogue",
            List.of(
                "Helfer|Hallo! Heute gibt es leider keine Audienzen - der Petersdom ist geschlossen.",
                "Jakob|Bitte, es ist wirklich wichtig. Mir wird das Funding gestrichen, und nur der Papst kann mir noch helfen!",
                "Helfer|Das Funding... na gut. So viel Verzweiflung habe ich lange nicht gesehen.",
                "Helfer|Ich lasse dich ein. Der Heilige Vater erwartet dich drinnen am Altar - geh hinein."),
            obj -> obj instanceof String);

    POPE_SPAWN_X = builder.comment("X-Koordinate des Papst (Altar im Petersdom).").define("pope_spawn_x", -153);
    POPE_SPAWN_Y = builder.comment("Y-Koordinate (Höhe) des Papst - je nach Welt anpassen.").define("pope_spawn_y", 71);
    POPE_SPAWN_Z = builder.comment("Z-Koordinate des Papst.").define("pope_spawn_z", -152);

    POPE_DIALOGUE = builder
        .comment("Dialog mit dem Papst am Altar. Format je Zeile: \"Sprecher|Text\".",
                "Rechtsklick zeigt die nächste Zeile. Nach der letzten Zeile verwandelt er sich in Müller.")
        .defineList("pope_dialogue",
            List.of(
                "Papst|Mein Sohn. Man erzählt sich, du forschst über harmonische Analysis.",
                "Papst|Fourierreihen, Konvergenz, singuläre Integrale - ein erhabenes, aber gefährliches Feld.",
                "Papst|Die Harmonie der Schwingungen ist gottgegeben, doch nur wenige beherrschen sie wahrhaftig.",
                "Papst|Du möchtest also mein Dekret für dein Funding? Nun gut - du sollst es bekommen...",
                "Papst|...ABER nur, wenn du deinen eigenen Doktorvater im Kampf bezwingst!",
                "Papst|Erblicke die wahre Gestalt von Paul Müller!"),
            obj -> obj instanceof String);

    VICTORY_TELEPORT_DELAY_SECONDS = builder
        .comment("Sekunden NACH dem Aufheben der PhD-Schriftrolle, bevor der Spieler zur",
                "Geburtstagsplattform teleportiert wird (kurze Verschnaufpause nach dem Sieg).")
        .define("victory_teleport_delay_seconds", 5);

    VICTORY_FALLBACK_SECONDS = builder
        .comment("Sicherheitsnetz: Wird die Schriftrolle nach dem Tod des Königs nicht aufgehoben,",
                "wird der Spieler spätestens nach so vielen Sekunden trotzdem teleportiert.")
        .define("victory_fallback_seconds", 120);
    VICTORY_PLATFORM_X = builder.comment("X-Koordinate der Geburtstagsplattform.").define("victory_platform_x", -332);
    VICTORY_PLATFORM_Y = builder.comment("Y-Koordinate der Geburtstagsplattform.").define("victory_platform_y", 136);
    VICTORY_PLATFORM_Z = builder.comment("Z-Koordinate der Geburtstagsplattform.").define("victory_platform_z", -151);

    builder.pop();
    SPEC = builder.build();
  }
}
