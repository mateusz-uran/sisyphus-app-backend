package io.github.mateuszuran.sisyphus_app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "work_group")
public class WorkGroup {
    @Id
    private String id;

    private Binary cv_url;
    private String creationTime;
    private int applied;
    private int denied;
    private int inProgress;

    @DocumentReference(lazy = true)
    private List<WorkApplications> workApplications;
}
