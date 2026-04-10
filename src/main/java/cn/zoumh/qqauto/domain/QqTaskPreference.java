package cn.zoumh.qqauto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "qa_qq_task_preference")
public class QqTaskPreference extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qq_account_id", nullable = false)
    private QqAccount qqAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_catalog_id", nullable = false)
    private TaskCatalog taskCatalog;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskExecutionStatus lastStatus = TaskExecutionStatus.PENDING;
}
