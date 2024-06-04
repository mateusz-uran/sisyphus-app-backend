package io.github.mateuszuran.sisyphus_app.dto;

import lombok.Builder;
import org.bson.types.Binary;

@Builder
public record WorkGroupDTO(String id, Binary cv_url, String creationTime, int applied, int denied, int inProgress) {}
