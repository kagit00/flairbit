package com.dating.flairbit.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MatchingGroupConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String type;
    private boolean active;
    private String intent;
}
