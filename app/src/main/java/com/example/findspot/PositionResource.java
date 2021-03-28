package com.example.findspot;

import java.util.HashMap;
import java.util.List;

public class PositionResource {
    private HashMap<String, Object> meta;
    private List<Documents> documents;

    public List<Documents> getDocuments() { return documents; }
}

class Documents {
    private HashMap<String, Object> address;
    private String address_type;
    private Double x;
    private Double y;
    private String address_name;
    private HashMap<String, Object> road_address;

    public Double getX() {  return x; }
    public Double getY() {  return y; }
}
