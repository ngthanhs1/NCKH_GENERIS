package com.example.servingwebcontent.Service;

import java.util.List;
import java.util.Optional;

/**
 * Generic Service Interface - Pattern CRUD cơ bản
 * Mục đích: Định nghĩa các operations chuẩn cho mọi loại Entity
 * @param <T> Kiểu Entity (ví dụ: User, Product, Node)
 * @param <ID> Kiểu ID của Entity (ví dụ: Long, String, UUID)
 */
public interface BaseService<T, ID> {
    /** Lấy tất cả entities */
    List<T> findAll();

    /** Tìm entity theo ID, trả về Optional để tránh null */
    Optional<T> findById(ID id);

    /** Lưu entity (tạo mới hoặc update) */
    T save(T entity);

    /** Xóa entity theo ID */
    void delete(ID id);
}
