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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "qa_qq_account")
public class QqAccount extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 32)
    private String qqNumber;

    @Column(nullable = false, length = 128)
    private String qqPassword;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(nullable = false, length = 64)
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private QqAccountStatus status = QqAccountStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime expireAt;

    private LocalDateTime lastRunAt;

    @Column(nullable = false)
    private boolean onlineFlag = true;
}
