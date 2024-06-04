package io.github.mateuszuran.sisyphus_app.repository;

import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkGroupRepository extends MongoRepository<WorkGroup, String> {
}
