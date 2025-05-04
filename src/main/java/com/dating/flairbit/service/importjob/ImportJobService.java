package com.dating.flairbit.service.importjob;

import com.dating.flairbit.dto.NodeExchange;

public interface ImportJobService {
    void startMatchesImport(NodeExchange payload);
}
