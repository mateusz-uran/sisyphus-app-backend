package io.github.mateuszuran.sisyphus_app.dto;

import lombok.Builder;

@Builder
public record WorkGroupDTO(String id, String cvData, String cvFileName, String creationTime, int applied, int denied, int inProgress) {}
