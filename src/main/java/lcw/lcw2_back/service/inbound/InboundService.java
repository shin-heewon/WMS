package lcw.lcw2_back.service.inbound;

import lcw.lcw2_back.dto.inbound.InboundDTO;
import lcw.lcw2_back.dto.inbound.page.PageInboundRequestDTO;
import lcw.lcw2_back.dto.inbound.page.PageInboundResponseDTO;

import java.util.List;

public interface InboundService {
    //입고요청서 작성
    void registerInbound(InboundDTO inboundDTO);

    //체크박스에서 체크한 요청 승인 service
    void modifyInboundApprove(List<Long> inboundIds);

    //체크박스에서 체크한 요청 승인 service
    void modifyInboundRejected(List<Long> inboundIds);

    //입고요청서 전체 조회
    PageInboundResponseDTO<InboundDTO> getInboundNotDoneList(PageInboundRequestDTO pageInboundRequestDTO);

    //입고목록(처리) 전체 조회
    PageInboundResponseDTO<InboundDTO> getInboundDoneList(PageInboundRequestDTO pageInboundRequestDTO);
}
