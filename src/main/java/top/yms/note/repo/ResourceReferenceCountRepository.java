package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.ResourceReferenceCount;

public interface ResourceReferenceCountRepository extends MongoRepository<ResourceReferenceCount, String> {

    ResourceReferenceCount findByResourceId(String resourceId);

}
