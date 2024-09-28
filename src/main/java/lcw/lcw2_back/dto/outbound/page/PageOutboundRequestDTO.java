package lcw.lcw2_back.dto.outbound.page;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageOutboundRequestDTO {
    @Builder.Default
    @Min(1)
    private int page = 1;

    @Builder.Default
    private int size = 15;
    private String status; //검색조건 : 진행상태
    private String item; //검색조건 : 품목명
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; //검색조건 : 요청날짜 중 시작날짜
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; //검색조건 : 요청날짜 중 끝날짜
    private String arriveName; //검색조건 : 수신지
    private String outboundStorageName; //검색조건 : 출고 창고 (총관리자만 사용)
    private boolean outboundMart; //검색조건 : 마트출고여부


    public Pageable getPageable(String...props) {
        return PageRequest.of(this.page -1, this.size, Sort.by(props).descending());
    }

    private String link;

    public String getLink() {

        if(link == null){
            StringBuilder builder = new StringBuilder();

            builder.append("page=" + this.page);

            builder.append("&size=" + this.size);
        }

        return link;
    }

    public int getSkip(){
        return (page-1)*15;
    }
}
