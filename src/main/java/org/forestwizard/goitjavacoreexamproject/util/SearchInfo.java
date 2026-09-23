package org.forestwizard.goitjavacoreexamproject.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SearchInfo {
    private String title;
    private LocalDateTime dateTime;
}
