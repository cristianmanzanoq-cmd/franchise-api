package co.com.nequi.franchise.mongo.repository;

import co.com.nequi.franchise.mongo.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FranchiseDataRepository extends ReactiveMongoRepository<FranchiseDocument, String> {
}
