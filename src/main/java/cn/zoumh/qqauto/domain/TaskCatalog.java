package cn.zoumh.qqauto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "qa_task_catalog")
public class TaskCatalog extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String taskCode;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 16)
    private String icon;

    @Column(nullable = false, length = 16)
    private String difficultyTag;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false)
    private Integer sortOrder;
}
