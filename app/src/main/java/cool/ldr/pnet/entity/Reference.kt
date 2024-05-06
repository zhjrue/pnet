package cool.ldr.pnet.entity

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.ToString

@Getter
@Setter
@ToString //生成空参构造方法
@NoArgsConstructor //生成全参构造方法
@AllArgsConstructor
class Reference {
    val id: String = ""
    val ref: String = ""
    val title: String = ""
    val DOI: String = ""
    val material_type: String = ""
    val comment: String = ""
}
