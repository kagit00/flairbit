package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeExchange {
    private UUID domainId;
    private String groupId;
    private String fileContent;
    private String fileName;
    private String contentType;
    private NodeType type;
    private List<String> referenceIds; // non-cost based nodes
}