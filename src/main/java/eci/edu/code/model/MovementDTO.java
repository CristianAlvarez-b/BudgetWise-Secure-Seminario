package eci.edu.code.model;

import java.time.LocalDateTime;

public class MovementDTO {
    private Long id;
    private String name;
    private double value;
    private LocalDateTime date;
    private String type;

    public MovementDTO(Long id, String name, Double value, LocalDateTime date, String type) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.date = date;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}