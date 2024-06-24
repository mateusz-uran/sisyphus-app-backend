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

    private Binary cvData;
    private String cvFileName;
    private String creationTime;
    private int send;
    private int denied;
    private int inProgress;
    private boolean isHired;

    @DocumentReference(lazy = true)
    private List<WorkApplications> workApplications;
}
