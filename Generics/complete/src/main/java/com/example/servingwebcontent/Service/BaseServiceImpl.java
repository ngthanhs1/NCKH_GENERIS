package com.example.servingwebcontent.Service;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;

/**
 * Generic Service Implementation - Triển khai CRUD cơ bản
 * Mục đích: Cung cấp implementation mặc định cho BaseService, tránh lặp code
 * Các Service cụ thể chỉ cần extend class này và thêm business logic riêng
 * 
 * @param <T> Kiểu Entity
 * @param <ID> Kiểu ID
 */
public abstract class BaseServiceImpl<T, ID> implements BaseService<T, ID> {
    // Repository để tương tác với Neo4j database
    protected final Neo4jRepository<T, ID> repository;

    /**
     * Constructor - nhận repository từ subclass
     * @param repository Neo4j repository tương ứng với Entity T
     */
    protected BaseServiceImpl(Neo4jRepository<T, ID> repository) {
        this.repository = repository;
    }

    /**
     * Lấy tất cả entities từ database
     * @return List chứa tất cả entities
     */
    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    /**
     * Tìm entity theo ID
     * @param id ID của entity cần tìm
     * @return Optional chứa entity nếu tìm thấy, hoặc empty nếu không
     */
    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    /**
     * Lưu entity vào database (create hoặc update)
     * @param entity Entity cần lưu
     * @return Entity đã được lưu (có thể có ID mới nếu là create)
     */
    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    /**
     * Xóa entity khỏi database theo ID
     * @param id ID của entity cần xóa
     */
    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }
}
