package com.dating.flairbit.config.factory;

import com.dating.flairbit.dto.enums.NodeType;

import java.util.Map;

public interface ResponseFactory<T> {
    T createResponse(NodeType type, String referenceId, Map<String, String> metadata, String groupId);
}