package com.lhalj.emos.api.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.validator.constraints.Range;

@Data
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
public class SearchMonthCheckinForm {

    @NonNull
    @Range(min = 2000,max = 3000)
    private Integer year;

    @Range(min = 1,max = 12)
    private Integer month;
}
