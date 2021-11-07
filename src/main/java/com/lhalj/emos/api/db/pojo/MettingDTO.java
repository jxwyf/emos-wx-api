package com.lhalj.emos.api.db.pojo;

import lombok.Data;

@Data
public class MettingDTO {
    private String id, uuid, title, name, date, place, start, end, type, status, desc, photo, hour;
}
