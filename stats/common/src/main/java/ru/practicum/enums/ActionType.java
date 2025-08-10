package ru.practicum.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    ACTION_VIEW(0.4d),
    ACTION_REGISTER(0.8d),
    ACTION_LIKE(1.0d);

    private final double weight;

    ActionType(double weight) {
        this.weight = weight;
    }

    public static double getWeight(String type) {
        try {
            return ActionType.valueOf(type).getWeight();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong action type ", e);
        }
    }
}
