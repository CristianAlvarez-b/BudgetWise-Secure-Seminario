package eci.edu.code.model;

public class PocketDTO {
    private Long id;
    private String name;
    private Double value;
    private String color;

    public PocketDTO(Long id, String name, Double value, String color) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.color = color;
    }

    // Getters y Setters

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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
