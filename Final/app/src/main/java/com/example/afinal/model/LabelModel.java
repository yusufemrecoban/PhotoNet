package com.example.afinal.model;

public class LabelModel {
    String label;
    String description;

    public LabelModel() {
    }

    public LabelModel(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {return label;}

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
