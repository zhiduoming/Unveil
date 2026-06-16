package com.jieqi.ai.bot;

/** AI 难度等级。 */
public enum AiLevel {
    EASY("easy", "入门"),
    MEDIUM("medium", "标准"),
    HARD("hard", "挑战");

    private final String id;
    private final String label;

    AiLevel(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() { return id; }
    public String label() { return label; }

    public static AiLevel fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return MEDIUM;
        }
        String v = raw.trim().toLowerCase();
        for (AiLevel level : values()) {
            if (level.id.equals(v)) {
                return level;
            }
        }
        return MEDIUM;
    }
}
