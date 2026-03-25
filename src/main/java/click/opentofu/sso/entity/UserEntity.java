package click.opentofu.sso.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "user_index", columnList = "userIndex", unique = true)
    }
)
public class UserEntity {

    private String uuid;
    private String password;
    @Id private String emailId;
    private Boolean isEnabled;
    private Boolean isAdmin;
    private String dbTable;

    @Column(unique = true)
    private Integer userIndex;
}
