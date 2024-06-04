package io.github.mateuszuran.sisyphus_app.repository;

import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkApplicationsRepository extends MongoRepository<WorkApplications, String> {
}
